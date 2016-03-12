import java.awt.Color;


/**
 * Abstract parent class for objects that can occupy a cell in the Field
 */
public abstract class FieldOccupant extends Thread
         implements Runnable, Comparable<FieldOccupant>
{
   /**
    * 
    * @param row
    * @param col
    * @param theField
    */
   public FieldOccupant(int row, int col, Field theField)
   {
      setRow(row);
      setCol(col);
      setTheField(theField);
   }


   /**
    * @return the color to use for a cell containing a particular kind of
    *         occupant
    */
   abstract public Color getDisplayColor();


   /**
    * @return the row of this FieldOccupant.
    */
   public int getRow()
   {
      return _row;
   }


   /**
    * @param row
    *           the row to set for this FieldOccupant.
    */
   public void setRow(int row)
   {
      _row = row;
   }


   /**
    * @return the col of this FieldOccupant.
    */
   public int getCol()
   {
      return _col;
   }


   /**
    * @param col
    *           the col to set of this FieldOccupant.
    */
   public void setCol(int col)
   {
      _col = col;
   }


   /**
    * @return the Field
    */
   public Field getTheField()
   {
      return _theField;
   }


   /**
    * @param field
    *           the Field to set
    */
   public void setTheField(Field theField)
   {
      _theField = theField;
   }


   /**
    * Compares this FieldOccupant to the FieldOccupant passed as an argument.
    */
   @Override
   public int compareTo(FieldOccupant o)
   {
      return getRow() - o.getRow() == 0 ? getCol() - o.getCol()
               : Integer.MAX_VALUE;
   }

   protected static final int DEFAULT_SLEEP          = 750;
   protected static final int DEFAULT_SLEEP_VARIABLE = 500;
   private int                _row;
   private int                _col;
   private Field              _theField;
}
