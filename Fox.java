import java.awt.Color;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.Phaser;
import java.util.concurrent.PriorityBlockingQueue;


/**
 * Foxes can display themselves
 */
public class Fox extends FieldOccupant
{
   /**
    * Creates a new Fox.
    * 
    * @param row
    *           the row the Fox is located at.
    * @param col
    *           the col the Fox is located at.
    * @param theField
    *           the Field the Fox is on.
    * @param startPhaser
    *           the start phaser to wait on.
    */
   public Fox(int row, int col, Field theField, Phaser startPhaser)
   {
      super(row, col, theField, startPhaser);
   }


   /**
    * @return the Color to use for a Cell occupied by a Fox
    */
   @Override
   public Color getDisplayColor()
   {
      return Color.GREEN;
   } // getDisplayColor


   /**
    * Returns the String representing a Fox.
    * 
    * @return the String representing a Fox.
    */
   @Override
   public String toString()
   {
      return "F";
   }


   /**
    * Allows a Foxes to reproduce with other Foxes.
    */
   @Override
   public void run()
   {
      Random r = new Random();
      Vector<Fox> foxes = new Vector<>();
      Vector<Cell<FieldOccupant>> emptyCells = new Vector<>();
      Fox toMate = null;
      Cell<FieldOccupant> foxhole = null;
      Cell<FieldOccupant> second, third;
      PriorityBlockingQueue<Cell<FieldOccupant>> toLock = new PriorityBlockingQueue<>();
      Field theField = getTheField();

      try
      {
         // If we haven't started, join and wait for the start signal...
         getStartPhaser().awaitAdvance(getStartPhaser().getPhase());

         // Foxes never die, so keep going until we get eaten.
         while (theField.getCellAt(getRow(), getCol())
                  .getOccupant() == this)
         {
            // Sleep for a fixed time plus a random time.
            Thread.sleep(r.nextInt(DEFAULT_SLEEP_VARIABLE) + DEFAULT_SLEEP);

            // Wipe out our lists for this run.
            foxes.clear();
            emptyCells.clear();

            // Now do Fox things...
            // Iterate over the neighbors and see if we have any empty Cells
            // nearby, if we find some, keep track of them.
            for (Cell<FieldOccupant> neighbor : theField
                     .getNeighborsOf(getRow(), getCol()))
            {
               // If a Cell is empty, keep a reference to it.
               if (neighbor.getOccupant() == null)
               {
                  emptyCells.addElement(neighbor);
               }
            } // for

            // If a Fox finds a neighboring empty Cell that has another
            // Fox as a neighbor and has at most one Hound as a neighbor
            // (note this could be a sleeping neighbor of the Fox itself!)
            // then a new Fox is born in that Cell.
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

                  // Prioritize our Cells.
                  toLock.put(theField.getCellAt(toMate.getRow(),
                           toMate.getCol()));
                  toLock.put(foxhole);

                  // Get the next two Cells to lock.
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
                                       foxhole.getCol(), theField,
                                       getStartPhaser()));
                              foxhole.getOccupant().start();

                           }
                        } // Release lock on third Lock
                     } // Release lock on second lock
                  } // Release lock on first lock
               } // If we have potential mates
            } // If we have empty Cells
         } // While we are still on the Field
      }
      catch (InterruptedException e)
      {
         // Make sure we don't exist on the field any more if we are
         // interrupted.
         synchronized (getTheField().getCellAt(getRow(), getCol()))
         {
            // If we are still on the Field, remove ourself.
            if (this == getTheField().getCellAt(getRow(), getCol())
                     .getOccupant())
            {
               getTheField().setCellAt(getRow(), getCol(), null);
            }
         }
      }
   }
}
