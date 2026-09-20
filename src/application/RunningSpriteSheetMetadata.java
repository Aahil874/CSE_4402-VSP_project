package application;
import java.util.EnumMap;
import java.util.Map;
/** Four complete alternating-stride poses per mascot in the v19 atlas. */
public final class RunningSpriteSheetMetadata {
    public static final String RESOURCE="/application/mascot-running-atlas-v19.png";
    public static final int ATLAS_COLUMNS=4,ATLAS_ROWS=12;
    public record Sheet(int frameWidth,int frameHeight,int columns,int rows,double frameSeconds,
        double footBaseline,double pivotX,double leftFootX,double rightFootX,double gloveX,double gloveY,double batX,double batY){}
    private static final Map<Mascot,Sheet> SHEETS=new EnumMap<>(Mascot.class);
    static{for(Mascot m:Mascot.values())SHEETS.put(m,new Sheet(181,181,4,1,.09,.96,.50,.38,.64,.72,.51,.54,.48));}
    private RunningSpriteSheetMetadata(){}
    public static Sheet forMascot(Mascot m){return SHEETS.get(m);}
    public static int rowIndex(Mascot m){return m.ordinal();}
    public static boolean valid(){
        for(Sheet s:SHEETS.values())if(s.columns()!=4||s.rows()!=1||s.frameWidth()!=181||s.frameHeight()!=181||s.leftFootX()>=s.rightFootX())return false;
        return SHEETS.size()==ATLAS_ROWS;
    }
}

