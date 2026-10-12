public class Main {

    public static void main(String[] args) {

        Physics sandbox = new Physics();

        new UI(
                Physics.WIDTH,
                Physics.HEIGHT,
                sandbox);
    }
}