package kessel.hex.orders.diplomat;

import kessel.hex.domain.Army;
import kessel.hex.domain.Diplomat;
import kessel.hex.domain.Game;
import kessel.hex.domain.GameTest;
import kessel.hex.domain.PopCenter;
import kessel.hex.orders.AbstractOrderTest;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import static org.junit.Assert.*;

/** Test inspiring loyalty in pops. */
public class DiplomatInspireLoyaltyTest extends AbstractOrderTest<DiplomatInspireLoyalty>
{
  private static final Logger LOG = Logger.getLogger( DiplomatInspireLoyaltyTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  @Test
  public void testPersistence() throws Exception
  {
    Game game = GameTest.createSimpleGame();
    Diplomat diplomatForA = game.getPlayers().get( 0 ).getDiplomats().get( 0 );
    PopCenter popForB = game.getPlayers().get( 1 ).getPopCenters().get( 0 );
    diplomatForA.setBase( popForB );

    DiplomatInspireLoyalty oldOrder = new DiplomatInspireLoyalty( diplomatForA );

    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    DiplomatInspireLoyalty newOrder = Game.GSON.fromJson( jsonOrder, DiplomatInspireLoyalty.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testIsNotInPop()
  {
    Game game = GameTest.createSimpleGame();
    Diplomat diplomatForA = game.getPlayers().get( 0 ).getDiplomats().get( 0 );
    Army armyForA = game.getPlayers().get( 0 ).getArmies().get( 0 );
    diplomatForA.setBase( armyForA );

    DiplomatInspireLoyalty order = new DiplomatInspireLoyalty( diplomatForA );
    order = Game.GSON.fromJson( Game.GSON.toJson( order ), DiplomatInspireLoyalty.class );
    order.fixDeserializationReferences( game );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testDiplomatIsGone()
  {
    Game game = GameTest.createSimpleGame();
    Diplomat diplomatForA = game.getPlayers().get( 0 ).getDiplomats().get( 0 );
    PopCenter popForB = game.getPlayers().get( 1 ).getPopCenters().get( 0 );
    diplomatForA.setBase( popForB );
    assertNotNull( game.getPlayers().get( 0 ).getDiplomat( diplomatForA.getId() ) );
    game.getPlayers().get( 0 ).remove( diplomatForA );
    assertNull( game.getPlayers().get( 0 ).getDiplomat( diplomatForA.getId() ) );

    DiplomatInspireLoyalty order = new DiplomatInspireLoyalty( diplomatForA );
    order = Game.GSON.fromJson( Game.GSON.toJson( order ), DiplomatInspireLoyalty.class );
    order.fixDeserializationReferences( game );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testInspiresLoyaltyImpact()
  {
    Game game = GameTest.createSimpleGame();
    Diplomat diplomatForA = game.getPlayers().get( 0 ).getDiplomats().get( 0 );
    Diplomat diplomatForB = game.getPlayers().get( 1 ).getDiplomats().get( 0 );
    PopCenter popForB = game.getPlayers().get( 1 ).getPopCenters().get( 0 );
    popForB.setLevel( 5 );
    diplomatForB.setLevel( 3 );
    diplomatForA.setBase( popForB );
    diplomatForB.setBase( popForB );

    DiplomatInspireLoyalty loyaltyOrder = new DiplomatInspireLoyalty( diplomatForB );
    assertEquals( 0, game.getCurrentTurn().getOrdersOfType( DiplomatInspireLoyalty.class ).size() );
    loyaltyOrder = Game.GSON.fromJson( Game.GSON.toJson( loyaltyOrder ), DiplomatInspireLoyalty.class );
    loyaltyOrder.fixDeserializationReferences( game );
    loyaltyOrder.execute( game );
    assertTrue( loyaltyOrder.wasExecuted() );
    assertEquals( 1, game.getCurrentTurn().getOrdersOfType( DiplomatInspireLoyalty.class ).size() );

    DiplomatInciteRebellion rebellionOrder = new DiplomatInciteRebellion( diplomatForA );
    assertEquals( 8, rebellionOrder.determineResistance( game ) );
  }
}

