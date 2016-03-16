/**
 * Generic helper class to control synchronized operations on a Field.
 */
public class Cell<T> implements Comparable<Cell<T>>
{
   /**
    * Creates a new Cell.
    * 
    * @param row
    *           the row of the Cell on a Field.
    * @param col
    *           the row of the Cell on a Field.
    */
   public Cell(int row, int col, T occupant)
   {
      setRow(row);
      setCol(col);
      setOccupant(occupant);
   }


   /**
    * Sets the col of the Cell.
    * 
    * @param col
    *           the col to set for the Cell.
    */
   public void setCol(int col)
   {
      p_col = col;
   }


   /**
    * Returns the col of the Cell.
    * 
    * @return the col of the Cell.
    */
   public int getCol()
   {
      return p_col;
   }


   /**
    * Returns the row of the Cell.
    * 
    * @return the row of the Cell.
    */
   public int getRow()
   {
      return p_row;
   }


   /**
    * Sets the row of the Cell.
    * 
    * @param row
    *           the row to set for the Cell.
    */
   public void setRow(int row)
   {
      p_row = row;
   }


   /**
    * Gets the FieldOccupant of this Cell.
    * 
    * @return the FieldOccupant of this Cell.
    */
   public T getOccupant()
   {
      return p_occupant;
   }


   /**
    * Sets the FieldOccupant of this Cell.
    * 
    * @param occupant
    *           the occupant of this Cell.
    */
   public void setOccupant(T occupant)
   {
      p_occupant = occupant;
      Simulation.DISPLAY_FIELD.getAndSet(true);
   }


   /**
    * Returns a String representation of this Cell.
    * 
    * @return a String representation of this Cell.
    */
   @Override
   public String toString()
   {
      return getOccupant() == null ? " " : getOccupant().toString();
   }


   /**
    * Returns a comparison of this Cell to the Cell passed as a parameter.
    * 
    * @param o
    *           the Cell to compare this Cell to.
    * @return positive if this Cell has a greater value than the Cell passed as
    *         an parameter, 0 if they are equal, negative otherwise.
    */
   @Override
   public int compareTo(Cell<T> o)
   {
      return getRow() - o.getRow() == 0 ? getCol() - o.getCol()
               : getRow() - o.getRow();
   }

   // A Cell is located at coordinates and can hold an occupant.
   private int p_row;
   private int p_col;
   private T   p_occupant;
} // Cell