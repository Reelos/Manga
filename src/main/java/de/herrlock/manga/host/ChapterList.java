package de.herrlock.manga.host;

import static de.herrlock.manga.util.Logger.L;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;

import de.herrlock.manga.host.ChapterList.Chapter;

public abstract class ChapterList extends ArrayList<Chapter> {

    /**
     * creates an instance of {@linkplain ChapterList}, gets the right {@linkplain Hoster} from the {@linkplain URL}
     * given as first argument
     * 
     * @param url
     *            the URL of the main-page with the list of chapters
     * @param pattern
     *            the selection of chapters
     * @return an instance of {@link ChapterList}; when no suitable Hoster can be detected {@code null}
     * @throws IOException
     *             thrown by {@link Hoster#getHoster(URL, String)}
     */
    public static ChapterList getInstance(URL url, String pattern) throws IOException {
        Hoster h = Hoster.getHostByURL(url);
        if (h != null)
            return h.getHoster(url, pattern);
        return null;
    }

    private final ChapterPattern cp;

    protected ChapterList(String pattern) {
        L.trace("new {0} ({1})", this.getClass(), pattern);
        this.cp = (pattern == null || pattern.equals("") ? null : new ChapterPattern(pattern));
    }

    public void addChapter(String number, URL chapterUrl) {
        if (this.cp == null || this.cp.contains(number))
            super.add(new Chapter(number, chapterUrl));
    }

    /**
     * returns the {@link URL} of one page
     * 
     * @param url
     * @return
     * @throws IOException
     */
    public abstract URL imgLink(URL url) throws IOException;

    /**
     * returns all page-{@link URL}s of one chapter
     * 
     * @param url
     *            the url of a chapter (mostly the first page)
     * @return
     * @throws IOException
     */
    public abstract Map<Integer, URL> getAllPageURLs(URL url) throws IOException;

    public Map<Integer, URL> getAllPageURLs(Chapter c) throws IOException {
        return getAllPageURLs(c.chapterUrl);
    }

    public static class Chapter {
        public final String number;
        public final URL chapterUrl;

        Chapter(String number, URL url) {
            this.number = number;
            this.chapterUrl = url;
        }

        @Override
        public String toString() {
            return MessageFormat.format("{0}: {1}", this.number, this.chapterUrl);
        }
    }

    private static class ChapterPattern extends ArrayList<String> {
        private static final long serialVersionUID = 1L;

        ChapterPattern(String pattern) {
            if (pattern.matches("([^;]+;?)+")) {
                for (String s : pattern.split(";")) {
                    String[] chapter = s.split("-");
                    if (chapter.length == 1)
                        super.add(s);
                    else if (chapter.length == 2) {
                        int first = Integer.parseInt(chapter[0]);
                        int last = Integer.parseInt(chapter[1]);
                        for (int i = first; i <= last; i++)
                            super.add(i + "");
                    }
                }
            }
        }
    }

    public static enum Hoster implements Comparator<Hoster> {
        Panda("Mangapanda", "http://www.mangapanda.com/"), //
        PureManga("PureManga", "http://www.pure-manga.org/"), //
        Fox("Mangafox", "http://www.mangafox.me/"), //

        ;

        private final String name;
        private final URL url;

        Hoster(String name, String url) {
            this.name = name;
            try {
                this.url = new URL(url);
            }
            catch (MalformedURLException ex) {
                throw new RuntimeException(ex);
            }
        }

        public String getName() {
            return this.name;
        }

        public URL getUrl() {
            return this.url;
        }

        /**
         * creates an instance of
         * 
         * @param mangaUrl
         *            the URL to the mainpage
         * @param chapterPattern
         *            the chapters to choose from
         * @return an instance of the ChapterList specified by the current {@link Hoster}-Object
         * @throws IOException
         *             thrown by the constructors of the special ChapterList-implementations
         */
        public ChapterList getHoster(URL mangaUrl, String chapterPattern) throws IOException {
            switch (this) {
                case Panda:
                    return new Panda(mangaUrl, chapterPattern);
                case PureManga:
                    return new PureManga(mangaUrl, chapterPattern);
                case Fox:
                    return new Fox(mangaUrl, chapterPattern);
                default:
                    throw new RuntimeException("Hoster \"" + this + "\" not found");
            }
        }

        @Override
        public int compare(Hoster o1, Hoster o2) {
            String o1Lower = o1.getName().toLowerCase(Locale.GERMAN);
            String o2Lower = o2.getName().toLowerCase(Locale.GERMAN);
            return o1Lower.compareTo(o2Lower);
        }

        public static Hoster getHostByURL(URL url) {
            for (Hoster h : Hoster.values()) {
                String hostUrlHost = h.getUrl().getHost(), givenUrlHost = url.getHost();
                if (hostUrlHost.matches("www\\..+"))
                    hostUrlHost = hostUrlHost.substring(4);
                if (givenUrlHost.matches("www\\..+"))
                    givenUrlHost = givenUrlHost.substring(4);
                if (hostUrlHost.equalsIgnoreCase(givenUrlHost))
                    return h;
            }
            return null;
        }

    }

}
