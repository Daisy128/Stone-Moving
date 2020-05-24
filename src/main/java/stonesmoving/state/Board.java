package stonesmoving.state;

public enum Board {

    STONE, //0
    UNFRAMED, //1
    FRAMED; //2

    public static Board of(int value) {
        if (value < 0 || value >= values().length) {
            throw new IllegalArgumentException();
        }
        return values()[value];
    }

    public int getValue() {
        return ordinal();
    }

    public String toString() {
        return Integer.toString(ordinal());
    }

}
