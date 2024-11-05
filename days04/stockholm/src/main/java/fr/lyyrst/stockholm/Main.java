package fr.lyyrst.stockholm;

public class Main {
    public static void main(String[] args) {
        Stockholm stockholm = new Stockholm("w7bPYdRh3Vq2mvFhLgZ5Vg==");
        switch (args.length) {
            case 0:
                stockholm.encrypt(false);
                break;
            case 1:
            case 2:
                stockholm.manageArgs(args);
                break;
            default:
                stockholm.printUsage(args);
                break;
        }
    }
}