import java.awt.Color;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.Phaser;
import java.util.concurrent.PriorityBlockingQueue;


/**
 * Hounds can display themselves. They also get hungry.
 */
public class Hound extends FieldOccupant
{
   /**
    * Creates a new Hound.
    * 
    * @param row
    *           the row the Hound is located at.
    * @param col
    *           the col the Hound is located at.
    * @param theField
    *           the Field the Hound is on.
    * @param startPhaser
    *           the start phaser to wait on.
    */
   public Hound(int row, int col, Field theField, Phaser startPhaser)
   {
      super(row, col, theField, startPhaser);

      // Start out well-fed
      eats();
   }


   /**
    * Returns true if this Hound has starved to death.
    * 
    * @return true if this Hound has starved to death.
    */
   public boolean hasStarved()
   {
      return p_fedStatus <= 0;
   }


   /**
    * Make this Hound hungrier by the amount we lived this round. Returns true
    * if the Hound has starved.
    *
    * @return true if this Hound has starved to death.
    */
   public boolean getHungrier(int timeLived)
   {
      // Decrease the fed status of this Hound
      p_fedStatus -= timeLived;
      return hasStarved();
   }


   /**
    * Feeds a Hound (i.e. resets hunger to start value).
    */
   public void eats()
   {
      // Reset the fed status of this Hound
      p_fedStatus = p_houndStarveTime;
   }


   /**
    * Returns the color to use for a Cell occupied by a Hound.
    * 
    * @return the color to use for a Cell occupied by a Hound
    */
   @Override
   public Color getDisplayColor()
   {
      float defaultHue = 0.0f;
      float defaultBrightness = 1.0f;

      return Color.getHSBColor(defaultHue,
               ((float) p_fedStatus / p_houndStarveTime),
               defaultBrightness);
   } // getDisplayColor


   /**
    * Returns the String representing a Hound.
    * 
    * @return the String representing a Hound.
    */
   @Override
   public String toString()
   {
      return "H";
   }


   /**
    * Sets the starve time for this class.
    *
    * @param starveTime
    *           the starve time for a Hound.
    */
   public static void setStarveTime(int starveTime)
   {
      p_houndStarveTime = starveTime;
   }


   /**
    * Returns the starve time for Hounds.
    * 
    * @return the starve time for Hounds.
    */
   public static long getStarveTime()
   {
      return p_houndStarveTime;
   }


   /**
    * Allows a Hound to eat Foxes, reproduce, and eventually die if it can't eat
    * frequently enough.
    */
   @Override
   public void run()
   {
      Random r = new Random();
      PriorityBlockingQueue<Cell<FieldOccupant>> toLock = new PriorityBlockingQueue<>();
      Field theField = getTheField();
      Vector<Fox> foxes = new Vector<>();
      Vector<Hound> hounds = new Vector<>();
      Fox toEat = null;
      Hound toMate = null;
      Cell<FieldOccupant> first, second, third;
      int sleepTime;

      // If we haven't started, join and wait for the start signal...
      getStartPhaser().awaitAdvance(getStartPhaser().getPhase());

      // While we haven't starved, we can keep on doing things.
      do
      {
         // Get the next sleep time for this Hound.
         sleepTime = r.nextInt(DEFAULT_SLEEP_VARIABLE) + DEFAULT_SLEEP;

         // Wait for the random amount of time.
         try
         {
            Thread.sleep(sleepTime);
         }
         catch (InterruptedException e)
         {
            // ignore, we can't be interrupted because we are a Hound.
         }

         // Now do Hound things...
         // Iterate over the neighbors and see how many foxes and
         // hounds are nearby
         for (Cell<FieldOccupant> neighbor : theField
                  .getNeighborsOf(getRow(), getCol()))
         {
            if (neighbor.getOccupant() instanceof Fox)
            {
               foxes.addElement((Fox) neighbor.getOccupant());
            }
            else if (neighbor.getOccupant() instanceof Hound)
            {
               hounds.addElement((Hound) neighbor.getOccupant());
            }
         } // for

         // Attempt to eat the Fox and then reproduce.
         if (foxes.size() > 0 && hounds.size() > 0)
         {
            // Get a random Fox to eat, then get a random Hound to mate
            // with.
            toEat = foxes.get(r.nextInt(foxes.size()));
            toMate = hounds.get(r.nextInt(hounds.size()));

            // Add both of the Cells we intend to use if we get the chance to
            // our priority queue so we can lock Cells in the correct order as
            // to avoid deadlock.
            toLock.add(theField.getCellAt(toEat.getRow(), toEat.getCol()));
            toLock.add(
                     theField.getCellAt(toMate.getRow(), toMate.getCol()));

            // For clarity of reading, we'll lock the second and third Cells
            // (after locking ourself first).
            second = toLock.poll();
            third = toLock.poll();

            // Lock the first Cell, always our Cell to avoid race conditions.
            synchronized (this)
            {
               // Lock the second Cell.
               synchronized (second)
               {
                  // Lock the third Cell.
                  synchronized (third)
                  {
                     // If the location is still occupied, and the
                     // occupant is a Fox, we'll eat that Fox.
                     if (theField.isOccupied(toEat.getRow(), toEat.getCol())
                              && theField
                                       .getCellAt(toEat.getRow(),
                                                toEat.getCol())
                                       .getOccupant() instanceof Fox)
                     {
                        // Set the Fox's Cell to be null so it will exit before
                        // trying to do anything.
                        theField.setCellAt(toEat.getRow(), toEat.getCol(),
                                 null);

                        // Feed ourself from the Fox we just ate.
                        eats();

                        // If the Neighboring Hound still exists, then
                        // birth a new Hound where the Fox was.
                        // Else we missed the Fox, so we just get hungrier.
                        if (theField.isOccupied(toMate.getRow(),
                                 toMate.getCol())
                                 && theField
                                          .getCellAt(toMate.getRow(),
                                                   toMate.getCol())
                                          .getOccupant() instanceof Hound)
                        {
                           // Set the Cell that we are going to eat with a new
                           // Hound and start the thread.
                           theField.setCellAt(toEat.getRow(),
                                    toEat.getCol(),
                                    new Hound(toEat.getRow(),
                                             toEat.getCol(), theField,
                                             getStartPhaser()))
                                    .start();
                        }
                     } // Eat Fox
                  } // Release lock on third Cell
               } // Release lock on second Cell
            } // Release lock on first Cell
         } // Eat Fox, spawn Hound

         // Else, we will just try to go eat a Fox.
         else if (foxes.size() > 0)
         {
            // Get a random Fox to eat.
            toEat = foxes.get(r.nextInt(foxes.size()));

            // Prioritize our Cells.
            toLock.add(theField.getCellAt(getRow(), getCol()));
            toLock.add(theField.getCellAt(toEat.getRow(), toEat.getCol()));

            // Get the next two Cells to lock.
            first = toLock.poll();
            second = toLock.poll();

            // Lock the first Cell.
            synchronized (first)
            {
               // Lock the second Cell.
               synchronized (second)
               {
                  // If the location is still occupied, and the occupant
                  // is a Fox, we'll eat that Fox.
                  // Else we missed the Fox, so get hungrier.
                  if (theField.isOccupied(toEat.getRow(), toEat.getCol())
                           && theField
                                    .getCellAt(toEat.getRow(),
                                             toEat.getCol())
                                    .getOccupant() instanceof Fox)
                  {
                     // Set the Fox's Cell to be null so it will exit before
                     // trying to do anything.
                     theField.setCellAt(toEat.getRow(), toEat.getCol(),
                              null);

                     // Feed ourself from the Fox we just ate.
                     eats();
                  }
               } // Release lock on second Cell
            } // Release lock on first Cell
         } // Just eat a Fox
           // Else If none of its neighbors is a Fox, it gets hungrier.

         // Reset our lists of Foxes and Hounds nearby.
         foxes.clear();
         hounds.clear();
      }
      while (!getHungrier(sleepTime));

      // Before exiting, remove ourselves from the Field.
      getTheField().setCellAt(getRow(), getCol(), null);
   } // run

   // Default starve time for Hounds
   public static final int DEFAULT_STARVE_TIME = DEFAULT_SLEEP
            + DEFAULT_SLEEP + DEFAULT_SLEEP;

   // Class variable for all hounds
   private static int p_houndStarveTime = DEFAULT_STARVE_TIME;

   // Instance attributes to keep track of how hungry we are
   private int p_fedStatus;

}
