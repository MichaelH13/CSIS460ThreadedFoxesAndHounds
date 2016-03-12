import java.awt.Color;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.PriorityBlockingQueue;


/**
 * Foxes can display themselves
 */
public class Fox extends FieldOccupant
{
   public Fox(int row, int col, Field theField)
   {
      super(row, col, theField);
      start();
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
         if (Field.START.get())
         {
            Random r = new Random();
            Field theField = getTheField();
            ConcurrentLinkedDeque<Cell<FieldOccupant>> neighbors;
            Vector<Fox> foxes = new Vector<>();
            Vector<Hound> hounds = new Vector<>();
            Vector<Cell<FieldOccupant>> emptyCells = new Vector<>();
            Fox toMate = null;
            Cell<FieldOccupant> foxhole = null;
            PriorityBlockingQueue<Cell<FieldOccupant>> toLock = new PriorityBlockingQueue<>();
            int sleepTime = r.nextInt(DEFAULT_SLEEP)
                     + DEFAULT_SLEEP_VARIABLE;

            // Foxes never die, so keep going until we get eaten.
            while (true)
            {
               Thread.sleep(sleepTime);

               // Get neighbors.
               neighbors = theField.getNeighborsOf(getRow(), getCol());

               // Now do Fox things...
               // Iterate over the neighbors and see how many foxes and
               // hounds are nearby
               for (Cell<FieldOccupant> neighbor : neighbors)
               {
                  // If the occupant is null, then we have an empty Cell.
                  if (neighbor.getOccupant() == null)
                  {
                     emptyCells.addElement(neighbor);
                  }
                  // Otherwise keep track of the Foxes/Hounds.
                  else if (neighbor.getOccupant() instanceof Fox)
                  {
                     foxes.addElement((Fox) neighbor.getOccupant());
                  }
                  else if (neighbor.getOccupant() instanceof Hound)
                  {
                     hounds.addElement((Hound) neighbor.getOccupant());
                  }
               } // for

               // If a Fox finds a neighboring empty cell that has another Fox
               // as a neighbor and has at most one Hound as a neighbor (note
               // this could be a sleeping neighbor of the Fox itself!) then a
               // new Fox is born in that cell.
               if (foxes.size() > 0 && emptyCells.size() > 0)
               {
                  // Get a random Fox to mate with.
                  toMate = foxes.get(r.nextInt(foxes.size()));
                  foxhole = emptyCells.get(r.nextInt(emptyCells.size()));

                  // Prepare to lock the Cells we will be manipulating.
                  toLock.put(theField.getCellAt(toMate.getRow(),
                           toMate.getCol()));
                  toLock.put(foxhole);
                  toLock.put(theField.getCellAt(getRow(), getCol()));

                  // Get the first lock.
                  synchronized (toLock.poll())
                  {
                     // Get the second lock.
                     synchronized (toLock.poll())
                     {
                        // Get the third lock.
                        synchronized (toLock.poll())
                        {
                           // Make sure we are still alive...
                           if (theField.getCellAt(getRow(), getCol())
                                    .getOccupant() == this)
                           {
                              // Then make sure our mate is still alive...
                              if (theField
                                       .getCellAt(toMate.getRow(),
                                                toMate.getCol())
                                       .getOccupant() instanceof Fox)
                              {
                                 // Finally make sure we are going to birth to
                                 // an empty Cell still.
                                 if (foxhole.getOccupant() == null)
                                 {
                                    foxhole.setOccupant(new Fox(
                                             foxhole.getRow(),
                                             foxhole.getCol(), theField));
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         } // if START
      }
      catch (InterruptedException e)
      {
         // we are dead.
      }
   }
}
