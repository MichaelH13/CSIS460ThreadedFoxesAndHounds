
/**
 * General helper class to control synchronized operations on a Field.
 */
public class Cell<T>
{
   /**
    * Creates a new Cell.
    * 
    * @param row
    *           The row of the Cell on a Field.
    * @param col
    *           The row of the Cell on a Field.
    */
   Cell(int row, int col, T occupant)
   {
      setRow(row);
      setCol(col);
      setOccupant(occupant);
   }


   /**
    * Sets the col of the Cell.
    * 
    * @param col
    *           The col to set for the Cell.
    */
   void setCol(int col)
   {
      p_col = col;
   }


   /**
    * Returns the col of the Cell.
    * 
    * @return The col of the Cell.
    */
   int getCol()
   {
      return p_col;
   }


   /**
    * Returns the row of the Cell.
    * 
    * @return The row of the Cell.
    */
   int getRow()
   {
      return p_row;
   }


   /**
    * Sets the row of the Cell.
    * 
    * @param row
    *           The row to set for the Cell.
    */
   void setRow(int row)
   {
      p_row = row;
   }


   /**
    * Gets the FieldOccupant of this Cell.
    * 
    * @return The FieldOccupant of this Cell.
    */
   public T getOccupant()
   {
      return p_occupant;
   }


   /**
    * Display a Cell.
    */
   @Override
   public String toString()
   {
      return getOccupant() == null ? " " : getOccupant().toString();
   }


   /**
    * Sets the FieldOccupant of this Cell.
    * 
    * @param occupant
    *           The occupant of this Cell.
    */
   public void setOccupant(T occupant)
   {
      p_occupant = occupant;
      Simulation.DISPLAY_FIELD.getAndSet(true);
   }

   private int p_row;
   private int p_col;
   private T   p_occupant;
} // Cell