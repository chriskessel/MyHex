package kessel.hex.orders.wizard;

import kessel.hex.domain.Diplomat;
import kessel.hex.domain.Game;
import kessel.hex.domain.GameTest;
import kessel.hex.domain.PopCenter;
import kessel.hex.domain.Wizard;
import kessel.hex.orders.AbstractOrderTest;
import kessel.hex.orders.diplomat.DiplomatInciteRebellion;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import static org.junit.Assert.*;

public class CharmRegionTest extends AbstractOrderTest<CharmRegion>
{
  private static final Logger LOG = Logger.getLogger( CharmRegionTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  @Test
  public void testPersistence() throws Exception
  {
    Game game = GameTest.createSimpleGame();
    Wizard wizardForA = game.getPlayers().get( 0 ).getWizards().get( 0 );

    CharmRegion oldOrder = new CharmRegion( wizardForA );
    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    CharmRegion newOrder = Game.GSON.fromJson( jsonOrder, CharmRegion.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testCharmRegionBlockedByShield()
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
    wizardForA.setLevel( 2 );
    CharmRegion charmRegionOrder = new CharmRegion( wizardForA );
    charmRegionOrder.execute( game );
    assertFalse( charmRegionOrder.wasExecuted() );
  }

  @Test
  public void testCharmRegionImpact()
  {
    // Cast the charm spell.
    Game game = GameTest.createSimpleGame();
    Wizard wizardForB = game.getPlayers().get( 1 ).getWizards().get( 0 );
    wizardForB.setLevel( 3 );
    CharmRegion charmRegionOrder = new CharmRegion( wizardForB );
    charmRegionOrder.execute( game );
    assertEquals( 1, game.getCurrentTurn().getOrdersOfType( CharmRegion.class ).size() );

    // Try the diplomatic order.
    Diplomat diplomatForA = game.getPlayers().get( 0 ).getDiplomats().get( 0 );
    PopCenter popForB = game.getPlayers().get( 1 ).getPopCenters().get( 0 );
    popForB.setLevel( 5 );
    diplomatForA.setBase( popForB );

    DiplomatInciteRebellion rebellionOrder = new DiplomatInciteRebellion( diplomatForA );
    assertEquals( wizardForB.getLevel(), charmRegionOrder.getCharmLevel() );
    assertEquals( 5 + charmRegionOrder.getCharmLevel(), rebellionOrder.determineResistance( game ) );
  }
}
