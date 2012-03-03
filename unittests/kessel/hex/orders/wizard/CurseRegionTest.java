package kessel.hex.orders.wizard;

import kessel.hex.domain.Army;
import kessel.hex.domain.ArmyUnit;
import kessel.hex.domain.Game;
import kessel.hex.domain.GameTest;
import kessel.hex.domain.Player;
import kessel.hex.domain.Wizard;
import kessel.hex.orders.AbstractOrderTest;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import static org.junit.Assert.*;

public class CurseRegionTest extends AbstractOrderTest<CurseRegion>
{
  private static final Logger LOG = Logger.getLogger( CurseRegionTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  @Test
  public void testPersistence() throws Exception
  {
    Game game = GameTest.createSimpleGame();
    Wizard wizardForA = game.getPlayers().get( 0 ).getWizards().get( 0 );

    CurseRegion oldOrder = new CurseRegion( wizardForA );
    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    CurseRegion newOrder = Game.GSON.fromJson( jsonOrder, CurseRegion.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testCurseRegionBlockedByShield()
  {
    // Cast the shield spell.
    Game game = GameTest.createSimpleGame();
    Wizard wizardForB = game.getPlayers().get( 1 ).getWizards().get( 0 );
    wizardForB.setLevel( 3 );
    ShieldRegion shieldOrder = new ShieldRegion( wizardForB );
    shieldOrder.execute( game );
    assertTrue( shieldOrder.wasExecuted() );
    assertEquals( 1, game.getCurrentTurn().getOrdersOfType( ShieldRegion.class ).size() );

    // Cast the charm spell.
    Wizard wizardForA = game.getPlayers().get( 0 ).getWizards().get( 0 );
    wizardForA.setBase( wizardForB.getBase() );
    wizardForA.setLevel( 4 );
    CurseRegion curseRegionOrder = new CurseRegion( wizardForA );
    curseRegionOrder.execute( game );
    assertFalse( curseRegionOrder.wasExecuted() );
  }

  @Test
  public void testCurseRegionImpact()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Wizard wizardForA = playerA.getWizards().get( 0 );
    for ( Army army : playerB.getActiveArmies() )
    {
      army.setLocation( wizardForA.getLocation() );
    }
    wizardForA.setLevel( 4 );
    CurseRegion curseOrder = new CurseRegion( wizardForA );
    curseOrder.execute( game );

    // Validate playerB got hit.
    for ( Army army : playerB.getActiveArmies() )
    {
      for ( ArmyUnit armyUnit : army.getUnits() )
      {
        assertEquals( 90, armyUnit.getMorale() );
        assertEquals( 900, armyUnit.getSize() );
      }
    }
  }
}
