package fr.lyyrst.stockholm;

public class Main {
    public static void main(String[] args) {
        String key = "w7bPYdRh3Vq2mvFhLgZ5Vg==";
        boolean silent = false;
        boolean reverse = false;
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-h":
                case "-help":
                    Stockholm.printHelp();
                    System.exit(0);
                case "-v":
                case "-version":
                    Stockholm.printVersion();
                    System.exit(0);
                case "-r":
                case "-reverse":
                    if (i + 1 < args.length) {
                        key = args[i + 1];
                        reverse = true;
                        i++;
                    } else {
                        System.out.println("Error: Missing key for -r option.");
                        System.exit(1);
                    }
                    break;
                case "-s":
                case "-silent":
                    silent = true;
                    break;
                default:
                    Stockholm.printUsage(args);
                    System.exit(1);
            }
        }
        Stockholm stockholm = new Stockholm(key, silent);
        if (reverse) stockholm.decrypt();
        else stockholm.encrypt();
    }
}