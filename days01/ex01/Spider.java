import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.HashSet;
import java.util.Set;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Spider {
    private static final int DEFAULT_DEPTH = 5;
    private static final String DEFAULT_PATH = "./data/";
    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".gif", ".bmp");

    private static final int TIMEOUT_MILLIS = 10000;
    private static final int REQUEST_DELAY = 1000;

    private boolean isRecursive;
    private int maxDepth;
    private Path downloadPath;
    private Set<String> visitedUrls;

    public Spider(int maxDepth, String downloadPath) {
        this.isRecursive = isRecursive;
        this.maxDepth = maxDepth;
        this.downloadPath = Paths.get(downloadPath);
        this.visitedUrls = new HashSet<>();
        try {
            Files.createDirectories(this.downloadPath);
        } catch (IOException e) {
            System.err.println("Failed to create download directory: " + e.getMessage());
        }
    }

    public void downloadImages(String url, int currentDepth) {
        if (currentDepth > maxDepth || visitedUrls.contains(url)) {
            return;
        }

        visitedUrls.add(url);

        try {
            Thread.sleep(REQUEST_DELAY);

            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36")
                    .timeout(TIMEOUT_MILLIS)
                    .get();

            Elements images = doc.select("img[src]");
            for (Element img : images) {
                String imgUrl = img.absUrl("src");
                if (isValidImageUrl(imgUrl)) {
                    downloadImage(imgUrl);
                }
            }

            if (isRecursive) {
                Elements links = doc.select("a[href]");
                for (Element link : links) {
                    String linkedUrl = link.absUrl("href");
                    downloadImages(linkedUrl, currentDepth + 1);
                }
            }

        } catch (IOException e) {
            System.err.println("Error accessing URL: " + url + " - " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Request delay interrupted: " + e.getMessage());
        }
    }

    private boolean isValidImageUrl(String url) {
        String lowerCaseUrl = url.toLowerCase();
        return SUPPORTED_EXTENSIONS.stream().anyMatch(lowerCaseUrl::endsWith);
    }

    private void downloadImage(String imgUrl) {
        String originalFileName = imgUrl.substring(imgUrl.lastIndexOf("/") + 1).split("\\?")[0]; // Remove query params
        String safeFileName = originalFileName.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");

        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        String uniqueFileName = timeStamp + "_" + safeFileName;
        Path filePath = downloadPath.resolve(uniqueFileName);

        int attempts = 3;
        while (attempts > 0) {
            try (InputStream in = new URL(imgUrl).openStream()) {
                Files.copy(in, filePath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Downloaded: " + imgUrl + " as " + uniqueFileName);
                break;
            } catch (IOException e) {
                attempts--;
                System.err.println("Failed to download image: " + imgUrl + " - Attempt "
                        + (3 - attempts) + " of 3. " + e.getMessage());

                if (attempts > 0) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        System.err.println("Retry delay interrupted: " + ie.getMessage());
                        return;
                    }
                } else {
                    System.err.println("Failed to download image after 3 attempts: " + imgUrl);
                }
            }
        }
    }

    public static void main(String[] args) {
        if (args.length < 2 || args.length > 7) {
            System.out.println("Usage: java Spider [-rlp] URL");
            return;
        }

        boolean isRecursive = false;
        int depth = DEFAULT_DEPTH;
        String path = DEFAULT_PATH;
        String url = args[args.length - 1];

        for (int i = 0; i < args.length - 1; i++) {
            switch (args[i]) {
                case "-r":
                    isRecursive = true;
                    break; // Recursive is enabled by default
                case "-l":
                    if (i + 1 < args.length - 1) {
                        depth = Integer.parseInt(args[++i]);
                    } else {
                        System.err.println("Depth level missing for -l option. Using default depth: 5");
                    }
                    break;
                case "-p":
                    if (i + 1 < args.length - 1) {
                        path = args[++i];
                    } else {
                        System.err.println("Path missing for -p option. Using default path: ./data");
                    }
                    break;
                default:
                    System.out.println("Unknown option: " + args[i]);
                    return;
            }
        }

        Spider spider = new Spider(isRecursive, depth, path);
        spider.downloadImages(url, 0);
    }
}
