import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * The Field class defines an object that models a Field that can contain
 * things.
 */
public class Field
{
   /**
    * Creates an empty field of given width and height.
    *
    * @param width
    *           of the field.
    * @param height
    *           of the field.
    * @param fieldUpdated
    *           the flag to set if we should redraw the Field.
    */
   public Field(int width, int height, AtomicBoolean fieldUpdated)
   {
      // Setup number of rows.
      p_occupants = new Vector<>(width);

      for (int i = 0; i < width; i++)
      {
         // Create the columns for the Field.
         p_occupants.add(new Vector<Cell<FieldOccupant>>(height));

         // Set each location to null.
         for (int j = 0; j < height; j++)
         {
            p_occupants.get(i).add(new Cell<FieldOccupant>(i, j, null));
         }
      }

      p_fieldUpdated = fieldUpdated;
   } // Field


   /**
    * Creates an empty field of given width and height.
    *
    * @param width
    *           of the field.
    * @param height
    *           of the field.
    */
   public Field(int width, int height)
   {
      // Call our general constructor, ignoring the drawField parameter.
      this(width, height, new AtomicBoolean(false));
   } // Field


   /**
    * Returns the width of the field.
    * 
    * @return the width of the field.
    */
   public int getWidth()
   {
      return p_occupants.size();
   } // getWidth


   /**
    * Returns the height of the field.
    * 
    * @return the height of the field.
    */
   public int getHeight()
   {
      return p_occupants.get(0).size();
   } // getHeight


   /**
    * Place a FieldOccupant in Cell (x, y) and return a reference to the
    * FieldOccupant.
    *
    * @param x
    *           is the x-coordinate of the Cell to place a FieldOccupant in.
    * @param y
    *           is the y-coordinate of the Cell to place a FieldOccupant in.
    * @param toAdd
    *           is the FieldOccupant to place.
    * 
    * @return the FieldOccupant we just set, or null if it was null.
    */
   public synchronized FieldOccupant setCellAt(int x, int y,
            FieldOccupant toAdd)
   {
      // First get the Cell, then updated the Occupant of the Cell.
      getCellAt(normalizeIndex(x, WIDTH_INDEX),
               normalizeIndex(y, !WIDTH_INDEX)).setOccupant(toAdd);

      // We updated the Field, so make sure our state indicates so.
      getAndSetFieldUpdated(true);

      return toAdd;
   } // setCellAt


   /**
    * Returns the Cell at the coordinates provided.
    * 
    * @param x
    *           is the x-coordinate of the Cell whose contents are queried.
    * @param y
    *           is the y-coordinate of the Cell whose contents are queried.
    *
    * @return the Cell at the coordinates provided.
    */
   public synchronized Cell<FieldOccupant> getCellAt(int x, int y)
   {
      return p_occupants.get(normalizeIndex(x, WIDTH_INDEX))
               .get(normalizeIndex(y, !WIDTH_INDEX));
   } // getCellAt


   /**
    * Returns true if this Cell is occupied.
    * 
    * @param x
    *           is the x-coordinate of the Cell whose contents are queried.
    * @param y
    *           is the y-coordinate of the Cell whose contents are queried.
    *
    * @return true if the Cell is occupied
    */
   public boolean isOccupied(int x, int y)
   {
      return getCellAt(x, y).getOccupant() != null;
   } // isOccupied


   /**
    * Returns a collection of the occupants of Cells adjacent to the given Cell.
    * 
    * @return a collection of the occupants of Cells adjacent to the given Cell.
    */
   public ArrayList<Cell<FieldOccupant>> getNeighborsOf(int x, int y)
   {
      // For any Cell there are 8 neighbors - left, right, above, below,
      // and the four diagonals. Define a collection of offset pairs that
      // we'll step through to access each of the 8 neighbors
      final int[][] indexOffsets = { { 0, 1 }, { 1, 0 }, { 0, -1 },
            { -1, 0 }, { 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 } };
      ArrayList<Cell<FieldOccupant>> neighbors = new ArrayList<>();

      // Iterate over the set of offsets, adding them to the x and y
      // indexes to check the neighboring Cells
      for (int[] offset : indexOffsets)
      {
         // If there's something at that location, add it to our
         // neighbor set
         neighbors.add(getCellAt(x + offset[0], y + offset[1]));
      }

      return neighbors;

   } // getNeighborsOf


   /**
    * Sets the state object for this Field.
    * 
    * @param fieldUpdated
    *           true if we should draw the Field, false otherwise.
    */
   public boolean getAndSetFieldUpdated(boolean fieldUpdated)
   {
      return p_fieldUpdated.getAndSet(fieldUpdated);
   } // setFieldUpdated


   /**
    * Normalize an index (positive or negative) by translating it to a legal
    * reference within the bounds of the field.
    *
    * @param index
    *           the index to normalize.
    * @param isWidth
    *           is true when normalizing a width reference, false if a height
    *           reference
    *
    * @return the normalized index value
    */
   private int normalizeIndex(int index, boolean isWidthIndex)
   {
      // Set the bounds depending on whether we're working with the
      // width or height (i.e., !width)
      int bounds = isWidthIndex ? getWidth() : getHeight();

      // If x is non-negative use modulo arithmetic to wrap around
      if (index >= 0)
      {
         return index % bounds;
      }
      // For negative values we convert to positive, modulus the bounds and
      // then subtract from the width (i.e., we count from bounds down to
      // 0. If we get say, -12 on a field 10 wide, we convert -12 to
      // 12, modulus with 10 to get 2 and then subtract that from 10 to get 8)
      else
      {
         return bounds - (-index % bounds);
      }
   } // normalizeIndex

   /**
    * Define any variables associated with a Field object here. These variables
    * MUST be private.
    */
   private final Vector<Vector<Cell<FieldOccupant>>> p_occupants;

   // Used in index normalizing method to distinguish between x and y
   // indices
   private static final boolean WIDTH_INDEX = true;

   private AtomicBoolean p_fieldUpdated;

}
