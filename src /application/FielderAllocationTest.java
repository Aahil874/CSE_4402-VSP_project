package application;

public final class FielderAllocationTest {
    public static void main(String[] args) {
        for (WorldPoint active : new WorldPoint[]{new WorldPoint(500, 300), new WorldPoint(520, 310)}) {
            WorldPoint ball = new WorldPoint(520, 310);
            for (boolean left : new boolean[]{false, true}) {
                WorldPoint backup = FielderAllocation.backupTarget(ball, active, left);
                check(active.distanceTo(backup) >= FielderAllocation.BACKUP_BUFFER, "Backup responder buffer");
                check(ball.distanceTo(backup) >= FielderAllocation.BACKUP_BUFFER, "Backup ball buffer");
            }
        }
        System.out.println("FIELDER_ALLOCATION=PASS");
    }
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
