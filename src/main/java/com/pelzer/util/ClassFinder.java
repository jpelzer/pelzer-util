package com.pelzer.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.pelzer.util.Logging.Logger;

/**
 * This utility class was based originally on <a
 * href="private.php?do=newpm&u=47838">Daniel Le Berre</a>'s <code>RTSI</code>
 * class. This class can be called in different modes, but the principal use is
 * to determine what subclasses/implementations of a given class/interface exist
 * in the current runtime environment.
 * 
 * @author Daniel Le Berre, Elliott Wade From:
 *         http://www.velocityreviews.com/forums
 *         /t137693-find-all-implementing-classes-in-classpath.html
 */
public class ClassFinder {
	private static final Logger log = Logging.getLogger(ClassFinder.class);
	private Class<?> searchClass = null;
	private Map<URL, String> classpathLocations = new HashMap<URL, String>();
	private Map<Class<?>, URL> results = new HashMap<Class<?>, URL>();
	private List<Throwable> errors = new ArrayList<Throwable>();
	private boolean verbose = true;

	public ClassFinder() {
		refreshLocations();
	}

	private void logVerbose(String message, Object... tokens) {
		if (verbose)
			log.debug(message, tokens);
	}

	/**
	 * Rescan the classpath, cacheing all possible file locations.
	 */
	public final void refreshLocations() {
		synchronized (classpathLocations) {
			classpathLocations = getClasspathLocations();
		}
	}

	/**
	 * @param fqcn
	 *          Name of superclass/interface on which to search
	 */
	public final Set<Class<?>> findSubclasses(String fqcn) {
		synchronized (classpathLocations) {
			synchronized (results) {
				searchClass = null;
				errors = new ArrayList<Throwable>();
				results = new TreeMap<Class<?>, URL>(CLASS_COMPARATOR);

				// filter malformed FQCN
				if (fqcn.startsWith(".") || fqcn.endsWith(".")) {
					return new HashSet<Class<?>>();
				}

				// Determine search class from fqcn
				try {
					searchClass = Class.forName(fqcn);
				} catch (ClassNotFoundException ex) {
					// if class not found, let empty vector return...
					errors.add(ex);
					return new HashSet<Class<?>>();
				}

				return findSubclasses(searchClass, classpathLocations);
			}
		}
	}

	public final List<Throwable> getErrors() {
		return new ArrayList<Throwable>(errors);
	}

	/**
	 * The result of the last search is cached in this object, along with the URL
	 * that corresponds to each class returned. This method may be called to query
	 * the cache for the location at which the given class was found.
	 * <code>null</code> will be returned if the given class was not found during
	 * the last search, or if the result cache has been cleared.
	 */
	public final URL getLocationOf(Class<?> cls) {
		if (results != null)
			return results.get(cls);
		else
			return null;
	}

	/**
	 * Determine every URL location defined by the current classpath, and it's
	 * associated package name.
	 */
	public final Map<URL, String> getClasspathLocations() {
		Map<URL, String> map = new TreeMap<URL, String>(URL_COMPARATOR);
		File file = null;

		String pathSep = System.getProperty("path.separator");
		String classpath = System.getProperty("java.class.path");
		logVerbose("classpath=" + classpath);

		StringTokenizer st = new StringTokenizer(classpath, pathSep);
		while (st.hasMoreTokens()) {
			String path = st.nextToken();
			file = new File(path);
			include(null, file, map);
		}

		return map;
	}

	/** Adds the given file into the map. If the file is a directory, */
	private final void include(String name, File file, Map<URL, String> map) {
		if (!file.exists())
			return;
		if (!file.isDirectory()) {
			// could be a JAR file
			includeJar(file, map);
			return;
		}

		if (name == null)
			name = "";
		else
			name += ".";

		// add subpackages
		File[] dirs = file.listFiles(DIRECTORIES_ONLY);
		for (int i = 0; i < dirs.length; i++) {
			try {
				// add the present package
				map.put(new URL("file://" + dirs[i].getCanonicalPath()),
				    name + dirs[i].getName());
			} catch (IOException ioe) {
				return;
			}

			include(name + dirs[i].getName(), dirs[i], map);
		}
	}

	private void includeJar(File file, Map<URL, String> map) {
		if (file.isDirectory())
			return;

		URL jarURL = null;
		JarFile jar = null;
		try {
			jarURL = new URL("file:/" + file.getCanonicalPath());
			jarURL = new URL("jar:" + jarURL.toExternalForm() + "!/");
			JarURLConnection conn = (JarURLConnection) jarURL.openConnection();
			jar = conn.getJarFile();
		} catch (Exception e) {
			// not a JAR or disk I/O error
			// either way, just skip
			return;
		}

		if (jar == null || jarURL == null)
			return;

		// include the jar's "default" package (i.e. jar's root)
		map.put(jarURL, "");

		Enumeration<JarEntry> e = jar.entries();
		while (e.hasMoreElements()) {
			JarEntry entry = e.nextElement();

			if (entry.isDirectory()) {
				if (entry.getName().toUpperCase().equals("META-INF/"))
					continue;

				try {
					map.put(new URL(jarURL.toExternalForm() + entry.getName()),
					    packageNameFor(entry));
				} catch (MalformedURLException murl) {
					// whacky entry?
					continue;
				}
			}
		}
	}

	private static String packageNameFor(JarEntry entry) {
		if (entry == null)
			return "";
		String s = entry.getName();
		if (s == null)
			return "";
		if (s.length() == 0)
			return s;
		if (s.startsWith("/"))
			s = s.substring(1, s.length());
		if (s.endsWith("/"))
			s = s.substring(0, s.length() - 1);
		return s.replace('/', '.');
	}

  	private final void includeResourceLocations(String packageName,
  	    Map<URL, String> map) {
		try {
			Enumeration<URL> resourceLocations = ClassFinder.class.getClassLoader()
			    .getResources(getPackagePath(packageName));

			while (resourceLocations.hasMoreElements()) {
				map.put(resourceLocations.nextElement(), packageName);
			}
		} catch (Exception e) {
			// well, we tried
			errors.add(e);
			return;
		}
	}

	private final Set<Class<?>> findSubclasses(Class<?> superClass,
	    Map<URL, String> locations) {
		Set<Class<?>> v = new HashSet<Class<?>>();

		Set<Class<?>> w = null;

		Iterator<URL> it = locations.keySet().iterator();
		while (it.hasNext()) {
			URL url = it.next();
			logVerbose("{}-->{}", url, locations.get(url));

			w = findSubclasses(url, locations.get(url), superClass);
			if (w != null && (w.size() > 0))
				v.addAll(w);
		}

		return v;
	}

	private final Set<Class<?>> findSubclasses(URL location,
	    String packageName, Class<?> superClass) {
		logVerbose("looking in package '{}' for class '{}'", packageName,
		    superClass);

		synchronized (results) {

			// hash guarantees unique names...
			Map<Class<?>, URL> thisResult = new TreeMap<Class<?>, URL>(
			    CLASS_COMPARATOR);
			Set<Class<?>> v = new HashSet<Class<?>>(); // ...but return a vector

			String fqcn = searchClass.getName();

			Set<URL> knownLocations = new HashSet<URL>();
			knownLocations.add(location);
			// TODO: add getResourceLocations() to this list

			// iterate matching package locations...
			for (URL url:knownLocations){

				// Get a File object for the package
				File directory = new File(url.getFile());

				logVerbose("looking in {}" , directory);

				if (directory.exists()) {
					// Get the list of the files contained in the package
					String[] files = directory.list();
					for (int i = 0; i < files.length; i++) {
						// we are only interested in .class files
						if (files[i].endsWith(".class")) {
							// removes the .class extension
							String classname = files[i].substring(0, files[i].length() - 6);

							logVerbose("checking file {}" , classname);

							try {
								Class<?> c = Class.forName(packageName + "." + classname);
								if (superClass.isAssignableFrom(c)
								    && !fqcn.equals(packageName + "." + classname)) {
									thisResult.put(c, url);
								}
							} catch (ClassNotFoundException cnfex) {
								errors.add(cnfex);
								// System.err.println(cnfex);
							} catch (Exception ex) {
								errors.add(ex);
								// System.err.println (ex);
							}
						}
					}
				} else {
					try {
						// It does not work with the filesystem: we must
						// be in the case of a package contained in a jar file.
						JarURLConnection conn = (JarURLConnection) url.openConnection();
						// String starts = conn.getEntryName();
						JarFile jarFile = conn.getJarFile();

						// logVerbose("starts=" + starts);
						// logVerbose("JarFile=" + jarFile);

						Enumeration<JarEntry> e = jarFile.entries();
						while (e.hasMoreElements()) {
							JarEntry entry = e.nextElement();
							String entryname = entry.getName();

							// logVerbose("\tconsidering entry: " + entryname);

							if (!entry.isDirectory() && entryname.endsWith(".class")) {
								String classname = entryname.substring(0,
								    entryname.length() - 6);
								if (classname.startsWith("/"))
									classname = classname.substring(1);
								classname = classname.replace('/', '.');

								try {
									Class<?> c = Class.forName(classname);

									if (superClass.isAssignableFrom(c) && !fqcn.equals(classname)) {
										thisResult.put(c, url);
									}
								} catch (Exception exception) {
									errors.add(exception);
								} 
							}
						}
					} catch (IOException ioex) {
						errors.add(ioex);
					}
				}
			} 

			logVerbose("results = {}", thisResult);

			results.putAll(thisResult);

			Iterator<Class<?>> it = thisResult.keySet().iterator();
			while (it.hasNext()) {
				v.add(it.next());
			}
			return v;

		} // synch results
	}

	private final static String getPackagePath(String packageName) {
		// Translate the package name into an "absolute" path
		String path = new String(packageName);
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		path = path.replace('.', '/');

		// ending with "/" indicates a directory to the classloader
		if (!path.endsWith("/"))
			path += "/";

		// for actual classloader interface (NOT Class.getResource() which
		// hacks up the request string!) a resource beginning with a "/"
		// will never be found!!! (unless it's at the root, maybe?)
		if (path.startsWith("/"))
			path = path.substring(1, path.length());

		//logVerbose("package path={}", path);

		return path;
	}

	private final static FileFilter DIRECTORIES_ONLY = new FileFilter() {
		public boolean accept(File f) {
			if (f.exists() && f.isDirectory())
				return true;
			else
				return false;
		}
	};

	private final static Comparator<URL> URL_COMPARATOR = new Comparator<URL>() {
		public int compare(URL u1, URL u2) {
			return String.valueOf(u1).compareTo(String.valueOf(u2));
		}
	};

	private final static Comparator<Class<?>> CLASS_COMPARATOR = new Comparator<Class<?>>() {
		public int compare(Class<?> c1, Class<?> c2) {
			return String.valueOf(c1).compareTo(String.valueOf(c2));
		}
	};

}