package application;

import java.io.DataInputStream;
import java.io.InputStream;

public final class MascotAtlasValidationTest {
    private static final int EXPECTED_COLUMNS = 4;
    private static final int CELL_WIDTH = 320;
    private static final int CELL_HEIGHT = 384;
    private static final long PNG_SIGNATURE = 0x89504E470D0A1A0AL;
    private static final int IHDR = 0x49484452;

    private MascotAtlasValidationTest() {}

    public static void main(String[] args) throws Exception {
        try (InputStream resource = MascotAtlasValidationTest.class.getResourceAsStream(
                "/application/mascot-action-atlas-v16.png")) {
            require(resource != null, "The v16 mascot action atlas must be packaged.");
            DataInputStream input = new DataInputStream(resource);
            require(input.readLong() == PNG_SIGNATURE, "The action atlas must be a PNG.");
            require(input.readInt() == 13 && input.readInt() == IHDR,
                    "The PNG must begin with a valid IHDR chunk.");
            int width = input.readInt();
            int height = input.readInt();
            require(width == CELL_WIDTH * EXPECTED_COLUMNS,
                    "The action atlas must contain exactly four action columns.");
            require(height == CELL_HEIGHT * Mascot.values().length,
                    "The action atlas must contain one row for every mascot.");
        }
        System.out.println("MASCOT_ATLAS_VALIDATION=PASS");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
