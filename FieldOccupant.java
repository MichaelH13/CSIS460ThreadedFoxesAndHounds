import java.awt.Color;
import java.util.concurrent.Phaser;


/**
 * Abstract parent class for objects that can occupy a cell in the Field
 */
public abstract class FieldOccupant extends Thread
         implements Runnable, Comparable<FieldOccupant>
{
   /**
    * Creates a FieldOccupant.
    * 
    * @param row
    *           The row of this FieldOccupant.
    * @param col
    *           The col of this FieldOccupant.
    * @param theField
    *           The Field this FieldOccupant is on.
    */
   public FieldOccupant(int row, int col, Field theField,
            Phaser startPhaser)
   {
      setRow(row);
      setCol(col);
      setTheField(theField);
      setStartPhaser(startPhaser);
   }


   /**
    * Returns the color to use for a Cell containing a particular kind of
    * occupant. The Color returned may depend on the state of the FieldOccupant
    * itself.
    * 
    * @return The Color to use for a Cell containing a particular kind of
    *         occupant.
    */
   abstract public Color getDisplayColor();


   /**
    * Returns the row of this FieldOccupant.
    * 
    * @return The row of this FieldOccupant.
    */
   public int getRow()
   {
      return p_row;
   }


   /**
    * Sets the row of this FieldOccupant.
    * 
    * @param row
    *           The row to set for this FieldOccupant.
    */
   public void setRow(int row)
   {
      p_row = row;
   }


   /**
    * Returns the col of this FieldOccupant.
    * 
    * @return the col of this FieldOccupant.
    */
   public int getCol()
   {
      return p_col;
   }


   /**
    * Sets the col for this FieldOccupant.
    * 
    * @param col
    *           The col to set of this FieldOccupant.
    */
   public void setCol(int col)
   {
      p_col = col;
   }


   /**
    * Returns the Field this FieldOccupant is on.
    * 
    * @return The Field this FieldOccupant is on.
    */
   public Field getTheField()
   {
      return p_theField;
   }


   /**
    * Sets the Field.
    * 
    * @param field
    *           The Field to set.
    */
   public void setTheField(Field theField)
   {
      p_theField = theField;
   }


   /**
    * Returns the start Phaser.
    * 
    * @return The startPhaser.
    */
   public Phaser getStartPhaser()
   {
      return p_startPhaser;
   }


   /**
    * Sets the start Phaser.
    * 
    * @param startPhaser
    *           The start Phaser to set.
    */
   public void setStartPhaser(Phaser startPhaser)
   {
      p_startPhaser = startPhaser;
   }


   /**
    * Compares this FieldOccupant to the FieldOccupant passed as an argument.
    */
   @Override
   public int compareTo(FieldOccupant o)
   {
      // First check row, if row is equal, then return the comparison of the
      // col, otherwise return the comparison of the row.
      return getRow() - o.getRow() == 0 ? getCol() - o.getCol()
               : getRow() - o.getRow();
   }

   protected static final int DEFAULT_SLEEP          = 750;
   protected static final int DEFAULT_SLEEP_VARIABLE = 500;
   private int                p_row;
   private int                p_col;
   private Field              p_theField;
   private Phaser             p_startPhaser;
}
