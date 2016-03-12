import java.awt.Color;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.PriorityBlockingQueue;


/**
 * Hounds can display themselves. They also get hungry.
 */
public class Hound extends FieldOccupant
{
   /**
    * Create a hound
    */
   public Hound(int row, int col, Field theField)
   {
      super(row, col, theField);

      // Start out well-fed
      eats();
      start();
   }


   /**
    * @return true if this Hound has starved to death
    */
   public boolean hasStarved()
   {
      return p_fedStatus <= 0;
   }


   /**
    * Make this Hound hungrier
    *
    * @return true if the Hound has starved to death
    */
   public boolean getHungrier(int timeLived)
   {
      // Decrease the fed status of this Hound
      p_fedStatus -= timeLived;
      return hasStarved();
   }


   public void eats()
   {
      // Reset the fed status of this Hound
      p_fedStatus = p_houndStarveTime;
   }


   /**
    * @return the color to use for a cell occupied by a Hound
    */
   @Override
   public Color getDisplayColor()
   {
      return Color.red;
   } // getDisplayColor


   /**
    * @return the text representing a Hound
    */
   @Override
   public String toString()
   {
      return "H";
   }


   /**
    * Sets the starve time for this class
    *
    * @param starveTime
    */
   public static void setStarveTime(int starveTime)
   {
      p_houndStarveTime = starveTime;
   }


   /**
    * @return the starve time for Hounds
    */
   public static long getStarveTime()
   {
      return p_houndStarveTime;
   }

   // Default starve time for Hounds
   public static final int DEFAULT_STARVE_TIME = DEFAULT_SLEEP * 4;

   // Class variable for all hounds
   private static int p_houndStarveTime = DEFAULT_STARVE_TIME;

   // Instance attributes to keep track of how hungry we are
   private int p_fedStatus;


   @Override
   public void run()
   {
      while (!hasStarved())
      {
         if (Field.START.get())
         {
            Random r = new Random();
            Field theField = getTheField();
            ConcurrentLinkedDeque<Cell<FieldOccupant>> neighbors;
            Vector<Fox> foxes = new Vector<>();
            Vector<Hound> hounds = new Vector<>();
            Fox toEat = null;
            Hound toMate = null;
            FieldOccupant first, second, third;
            PriorityBlockingQueue<FieldOccupant> toLock = new PriorityBlockingQueue<>();
            int sleepTime = r.nextInt(DEFAULT_SLEEP)
                     + DEFAULT_SLEEP_VARIABLE;

            // Wait for the specified number of seconds...
            try
            {
               Thread.sleep(sleepTime);
               getHungrier(sleepTime);
               if (Simulation.DEBUG)
               {
                  System.out.println(Thread.currentThread().getName()
                           + " says: I'm hungrier...fed_status:"
                           + p_fedStatus + " At location\tx=" + getRow()
                           + "\ty=" + getCol());
               }
            }
            catch (InterruptedException e)
            {
               // ignore, keep on going.
            }

            // Get neighbors.
            neighbors = theField.getNeighborsOf(getRow(), getCol());

            // Now do Hound things...
            // Iterate over the neighbors and see how many foxes and
            // hounds are nearby
            for (Cell<FieldOccupant> neighbor : neighbors)
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

               toLock.add(toEat);
               toLock.add(toMate);
               toLock.add(this);

               first = toLock.poll();
               second = toLock.poll();
               third = toLock.poll();

               // Lock the first FieldOccupant.
               synchronized (first)
               {
                  if (Simulation.DEBUG)
                  {
                     Simulation.LOCKED_CELLS.put(theField.getCellAt(
                              first.getRow(), first.getCol()), true);
                  }
                  // Lock the second FieldOccupant.
                  synchronized (second)
                  {
                     if (Simulation.DEBUG)
                     {
                        Simulation.LOCKED_CELLS.put(theField.getCellAt(
                                 second.getRow(), second.getCol()), true);
                     }

                     // Lock the third FieldOccupant.
                     synchronized (third)
                     {
                        if (Simulation.DEBUG)
                        {
                           Simulation.LOCKED_CELLS.put(theField.getCellAt(
                                    third.getRow(), third.getCol()), true);
                        }
                        // If the location is still occupied, and the
                        // occupant is a Fox, we'll eat that Fox.
                        if (theField.isOccupied(toEat.getRow(),
                                 toEat.getCol())
                                 && theField
                                          .getCellAt(toEat.getRow(),
                                                   toEat.getCol())
                                          .getOccupant() instanceof Fox)
                        {
                           // Eat the Fox.
                           eats();

                           // Tell the Fox it has been eaten.
                           toEat.notify(); // EXCEPTION?

                           // If the Neighboring Hound still exists, then
                           // birth a new Hound where the Fox was.
                           if (theField.isOccupied(toMate.getRow(),
                                    toMate.getCol())
                                    && theField
                                             .getCellAt(toMate.getRow(),
                                                      toMate.getCol())
                                             .getOccupant() instanceof Hound)
                           {
                              theField.getCellAt(toEat.getRow(),
                                       toEat.getCol()).setOccupant(
                                                new Hound(toEat.getRow(),
                                                         toEat.getCol(),
                                                         theField));
                           }
                        } // Eat Fox
                          // We missed the Fox, so get hungrier.
                        else
                        {
                           // getHungrier(sleepTime);
                        }
                     } // Lock third Cell.
                     if (Simulation.DEBUG)
                     {
                        Simulation.LOCKED_CELLS.put(theField.getCellAt(
                                 third.getRow(), third.getCol()), false);
                     }
                  } // Lock second Cell.
                  if (Simulation.DEBUG)
                  {
                     Simulation.LOCKED_CELLS.put(theField.getCellAt(
                              second.getRow(), second.getCol()), false);
                  }
               } // Lock first Cell.

               if (Simulation.DEBUG)
               {
                  Simulation.LOCKED_CELLS.put(theField.getCellAt(
                           first.getRow(), first.getCol()), false);
               }

            } // Eat Fox, spawn Hound.
              // Go eat the Fox.
            else if (foxes.size() > 0)
            {
               // Get a random Fox to eat.
               toEat = foxes.get(r.nextInt(foxes.size()));

               toLock.add(toEat);
               toLock.add(this);

               first = toLock.poll();
               second = toLock.poll();

               // Lock the first FieldOccupant.
               synchronized (first)
               {
                  if (Simulation.DEBUG)
                  {
                     Simulation.LOCKED_CELLS.put(theField.getCellAt(
                              first.getRow(), first.getCol()), true);
                  }
                  // Lock the second FieldOccupant.
                  synchronized (second)
                  {

                     if (Simulation.DEBUG)
                     {
                        Simulation.LOCKED_CELLS.put(theField.getCellAt(
                                 second.getRow(), second.getCol()), true);
                     }
                     // If the location is still occupied, and the occupant
                     // is a Fox, we'll eat that Fox.
                     if (theField.isOccupied(toEat.getRow(), toEat.getCol())
                              && theField
                                       .getCellAt(toEat.getRow(),
                                                toEat.getCol())
                                       .getOccupant() instanceof Fox)
                     {
                        // Eat the Fox.
                        eats();

                        // Tell the Fox it has been eaten.
                        theField.setCellAt(toEat.getRow(), toEat.getCol(),
                                 null);
                        toEat.notify();
                     }
                     // We missed the Fox, so get hungrier.
                     else
                     {
                        // getHungrier(sleepTime);
                     }
                  } // Lock second Cell.

                  if (Simulation.DEBUG)
                  {
                     Simulation.LOCKED_CELLS.put(theField.getCellAt(
                              second.getRow(), second.getCol()), false);
                  }
               } // Lock first Cell.

               if (Simulation.DEBUG)
               {
                  Simulation.LOCKED_CELLS.put(theField.getCellAt(
                           first.getRow(), first.getCol()), false);
               }
            }
            // If none of its neighbors is a Fox, it gets hungrier.
            else
            {
               // getHungrier(sleepTime);
            }
         }
      } // while(!hasStarved)

      // Before exiting, remove ourselves from the Field.
      getTheField().getCellAt(getRow(), getCol()).setOccupant(null);
   } // run

}
