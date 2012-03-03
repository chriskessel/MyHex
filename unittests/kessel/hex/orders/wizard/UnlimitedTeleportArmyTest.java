package kessel.hex.orders.wizard;

import kessel.hex.domain.Army;
import kessel.hex.domain.ArmyUnit;
import kessel.hex.domain.Game;
import kessel.hex.domain.GameTest;
import kessel.hex.domain.Player;
import kessel.hex.domain.TroopType;
import kessel.hex.domain.Wizard;
import kessel.hex.util.Tuple;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import static org.junit.Assert.*;

public class UnlimitedTeleportArmyTest extends AbstractTeleportArmyTest<UnlimitedTeleportArmy>
{
  private static final Logger LOG = Logger.getLogger( UnlimitedTeleportArmyTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  @Test
  public void testPersistence() throws Exception
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Wizard wizard = playerA.getWizards().get( 0 );
    Army army = playerA.getActiveArmies().get( 0 );
    wizard.setBase( army );
    UnlimitedTeleportArmy oldOrder = new UnlimitedTeleportArmy( wizard, new Tuple( 2, 2 ) );

    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    UnlimitedTeleportArmy newOrder = Game.GSON.fromJson( jsonOrder, UnlimitedTeleportArmy.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testTeleport()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Wizard wizard = playerA.getWizards().get( 0 );
    Army army = playerA.getActiveArmies().get( 0 );
    wizard.setBase( army );
    wizard.setLevel( 7 );
    while ( army.getUnits().size() <= wizard.getLevel() )
    {
      army.addUnit( new ArmyUnit( TroopType.LEVY ) );
    }
    assertTrue( army.getUnits().size() > wizard.getLevel() );
    UnlimitedTeleportArmy order = new UnlimitedTeleportArmy( wizard, new Tuple( 2, 2 ) );

    assertFalse( army.getLocation().getCoord().equals( new Tuple( 2, 2 ) ) );
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertEquals( army.getLocation().getCoord(), new Tuple( 2, 2 ) );
  }
}
