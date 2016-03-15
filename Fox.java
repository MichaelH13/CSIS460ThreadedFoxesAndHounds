import java.awt.Color;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.PriorityBlockingQueue;


/**
 * Foxes can display themselves
 */
public class Fox extends FieldOccupant
{
   public Fox(int row, int col, Field theField)
   {
      super(row, col, theField);
//      start();
   }


   /**
    * @return the color to use for a cell occupied by a Fox
    */
   @Override
   public Color getDisplayColor()
   {
      return Color.GREEN;
   } // getDisplayColor


   /**
    * @return the text representing a Fox
    */
   @Override
   public String toString()
   {
      return "F";
   }


   @Override
   public void run()
   {
      try
      {
         Field theField = getTheField();

         // While we are still on the Field.
         while (!Field.START.get())
         {
            // wait to start...
         }

         Random r = new Random();
         Vector<Fox> foxes = new Vector<>();
         Vector<Cell<FieldOccupant>> emptyCells = new Vector<>();
         Fox toMate = null;
         Cell<FieldOccupant> foxhole = null;
         Cell<FieldOccupant> second, third;
         PriorityBlockingQueue<Cell<FieldOccupant>> toLock = new PriorityBlockingQueue<>();

         // Foxes never die, so keep going until we get eaten.
         while (theField.getCellAt(getRow(), getCol())
                  .getOccupant() == this)
         {
            // Thread.sleep(r.nextInt(DEFAULT_SLEEP) + DEFAULT_SLEEP_VARIABLE);
            Thread.sleep(DEFAULT_SLEEP_VARIABLE);

            // Wipe out our lists for this run.
            foxes.clear();
            emptyCells.clear();

            // Now do Fox things...
            // Iterate over the neighbors and see if we have any empty Cells
            // nearby, if we find some, keep track of them.
            for (Cell<FieldOccupant> neighbor : theField
                     .getNeighborsOf(getRow(), getCol()))
            {
               if (neighbor.getOccupant() == null)
               {
                  emptyCells.addElement(neighbor);
               }
            } // for

            // If a Fox finds a neighboring empty cell that has another
            // Fox as a neighbor and has at most one Hound as a neighbor
            // (note this could be a sleeping neighbor of the Fox itself!)
            // then a new Fox is born in that cell.
            if (emptyCells.size() > 0)
            {
               // Get a random Empty Cell to mate in.
               foxhole = emptyCells.get(r.nextInt(emptyCells.size()));

               // Clear empty Cells so we don't get any duplicate empty Cells or
               // non-adjacent Cells from our last time checking for Empty
               // Cells.
               emptyCells.clear();

               // Iterate over the neighbors and see how many foxes and
               // hounds are nearby
               for (Cell<FieldOccupant> neighbor : theField
                        .getNeighborsOf(foxhole.getRow(), foxhole.getCol()))
               {
                  // If the occupant is null, then we have an empty Cell.
                  if (neighbor.getOccupant() == null)
                  {
                     emptyCells.addElement(neighbor);
                  }
                  // Otherwise keep track of the Foxes.
                  else if (neighbor.getOccupant() instanceof Fox
                           && neighbor.getOccupant() != this)
                  {
                     foxes.addElement((Fox) neighbor.getOccupant());
                  }
               } // for

               // Now that we've counted the Foxes, check to see if we have any
               // that share this empty Cell as an adjacent Cell.
               if (foxes.size() > 0)
               {
                  // Get a random Fox to mate with.
                  toMate = foxes.get(r.nextInt(foxes.size()));

                  // Prepare to lock the Cells we will be manipulating.
                  toLock.put(theField.getCellAt(toMate.getRow(),
                           toMate.getCol()));
                  toLock.put(foxhole);

                  second = toLock.poll();
                  third = toLock.poll();

                  // Get the first lock, always ourselves to avoid race
                  // conditions...
                  synchronized (this)
                  {
                     // Get the second lock
                     synchronized (second)
                     {
                        // Get the third lock
                        synchronized (third)
                        {
                           // Make sure we are still alive...
                           // Then make sure our mate is still alive...
                           // Finally make sure we are going to birth
                           // to an empty Cell still.
                           if (theField.getCellAt(getRow(), getCol())
                                    .getOccupant() == this
                                    && theField
                                             .getCellAt(toMate.getRow(),
                                                      toMate.getCol())
                                             .getOccupant() instanceof Fox
                                    && foxhole.getOccupant() == null)
                           {

                              foxhole.setOccupant(new Fox(foxhole.getRow(),
                                       foxhole.getCol(), theField));
                              foxhole.getOccupant().start();

                           }
                        } // Release third Lock
                     } // Release second lock
                  } // Release first lock
               } // If we have potential mates
            } // If START
         } // While we are still on the Field
      }
      catch (InterruptedException e)
      {
         // Make sure we don't exist on the field any more.
         getTheField().setCellAt(getRow(), getCol(), null);
      }
   }
}
