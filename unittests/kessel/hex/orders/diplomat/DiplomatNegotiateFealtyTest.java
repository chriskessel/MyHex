package kessel.hex.orders.diplomat;

import kessel.hex.domain.Army;
import kessel.hex.domain.Diplomat;
import kessel.hex.domain.Game;
import kessel.hex.domain.GameTest;
import kessel.hex.domain.Player;
import kessel.hex.domain.PopCenter;
import kessel.hex.map.Region;
import kessel.hex.orders.AbstractOrderTest;
import kessel.hex.util.Tuple;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import static org.junit.Assert.*;

/** Test taking over pops. */
@SuppressWarnings({ "AssignmentToStaticFieldFromInstanceMethod" })
public class DiplomatNegotiateFealtyTest extends AbstractOrderTest<DiplomatNegotiateFealty>
{
  private static final Logger LOG = Logger.getLogger( DiplomatNegotiateFealtyTest.class );

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

    DiplomatNegotiateFealty oldOrder = new DiplomatNegotiateFealty( diplomatForA );

    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    DiplomatNegotiateFealty newOrder = Game.GSON.fromJson( jsonOrder, DiplomatNegotiateFealty.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testIsNotInPop()
  {
    Game game = GameTest.createSimpleGame();
    Diplomat diplomatForA = game.getPlayers().get( 0 ).getDiplomats().get( 0 );
    Army armyForA = game.getPlayers().get( 0 ).getArmies().get( 0 );
    diplomatForA.setBase( armyForA );

    DiplomatNegotiateFealty order = new DiplomatNegotiateFealty( diplomatForA );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testPopIsSelfOwned()
  {
    Game game = GameTest.createSimpleGame();
    Diplomat diplomatForA = game.getPlayers().get( 0 ).getDiplomats().get( 0 );
    PopCenter popForA = game.getPlayers().get( 0 ).getPopCenters().get( 0 );
    diplomatForA.setBase( popForA );

    DiplomatNegotiateFealty order = new DiplomatNegotiateFealty( diplomatForA );
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

    DiplomatNegotiateFealty order = new DiplomatNegotiateFealty( diplomatForA );
    order = Game.GSON.fromJson( Game.GSON.toJson( order ), DiplomatNegotiateFealty.class );
    order.fixDeserializationReferences( game );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testDiplomatAlreadyDoneSomething()
  {
    Game game = GameTest.createSimpleGame();
    Diplomat diplomatForA = game.getPlayers().get( 0 ).getDiplomats().get( 0 );
    PopCenter popForB = game.getPlayers().get( 1 ).getPopCenters().get( 0 );
    diplomatForA.setBase( popForB );

    DiplomatNegotiateFealty order = new DiplomatNegotiateFealty( diplomatForA );
    diplomatForA.addOrderExecuted( order );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testNegotiateSucceedsNotSeen()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Diplomat diplomatForA = playerA.getDiplomats().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    PopCenter popForB = game.getPopCenter( new Tuple( 1, 1 ) );
    playerB.add( popForB );
    diplomatForA.setBase( popForB );

    DiplomatNegotiateFealty order = new DiplomatNegotiateFealty( diplomatForA )
    {
      protected boolean makeAttempt( Game game ) { return true; }
    };
    DiplomatNegotiateFealty.PERCENT_SEEN = 0;
    assertEquals( 22, playerB.getKnownItems().size() );
    order.execute( game );

    assertTrue( order.wasExecuted() );
    assertTrue( order.wasSuccessful() );
    assertFalse( playerB.getPopCenters().contains( popForB ) );
    assertEquals( playerA, popForB.getOwner() );
    assertEquals( 23, playerB.getKnownItems().size() );
    assertEquals( popForB, playerB.getKnownPopCenter( popForB.getId() ) );
    assertEquals( 1, playerA.getGameEvents().size() );
    assertEquals( 1, playerB.getGameEvents().size() );
  }

  @Test
  public void testNegotiateSucceedsIsSeen()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Diplomat diplomatForA = playerA.getDiplomats().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    PopCenter popForB = game.getPopCenter( new Tuple( 1, 1 ) );
    playerB.add( popForB );
    diplomatForA.setBase( popForB );

    DiplomatNegotiateFealty order = new DiplomatNegotiateFealty( diplomatForA )
    {
      protected boolean makeAttempt( Game game ) { return true; }
    };
    DiplomatNegotiateFealty.PERCENT_SEEN = 100;
    assertEquals( 22, playerB.getKnownItems().size() );
    assertEquals( 1, playerA.getPopCenters().size() );
    assertEquals( 2, playerB.getPopCenters().size() );
    order.execute( game );

    assertTrue( order.wasExecuted() );
    assertTrue( order.wasSuccessful() );
    assertFalse( playerB.getPopCenters().contains( popForB ) );
    assertEquals( playerA, popForB.getOwner() );
    assertEquals( 2, playerA.getPopCenters().size() );
    assertEquals( 1, playerB.getPopCenters().size() );
    assertEquals( 24, playerB.getKnownItems().size() );
    assertEquals( diplomatForA, playerB.getKnownItem( diplomatForA.getId() ) );
    assertEquals( popForB, playerB.getKnownPopCenter( popForB.getId() ) );
    assertEquals( 1, playerA.getGameEvents().size() );
    assertEquals( 1, playerB.getGameEvents().size() );
  }

  @Test
  public void testNegotiateFailsNotSeen()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Diplomat diplomatForA = playerA.getDiplomats().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    PopCenter popForB = game.getPopCenter( new Tuple( 1, 1 ) );
    playerB.add( popForB );
    diplomatForA.setBase( popForB );

    DiplomatNegotiateFealty order = new DiplomatNegotiateFealty( diplomatForA )
    {
      protected boolean makeAttempt( Game game ) { return false; }
    };
    DiplomatNegotiateFealty.PERCENT_SEEN = 0;
    assertEquals( 22, playerB.getKnownItems().size() );
    order.execute( game );

    assertTrue( order.wasExecuted() );
    assertFalse( order.wasSuccessful() );
    assertEquals( 1, playerA.getPopCenters().size() );
    assertEquals( 2, playerB.getPopCenters().size() );
    assertEquals( playerB, popForB.getOwner() );
    assertTrue( playerB.getPopCenters().contains( popForB ) );
    assertEquals( 22, playerB.getKnownItems().size() );
  }

  @Test
  public void testNegotiateFailsIsSeen()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Diplomat diplomatForA = playerA.getDiplomats().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    PopCenter popForB = game.getPopCenter( new Tuple( 1, 1 ) );
    playerB.add( popForB );
    diplomatForA.setBase( popForB );

    DiplomatNegotiateFealty order = new DiplomatNegotiateFealty( diplomatForA )
    {
      protected boolean makeAttempt( Game game ) { return false; }

      protected boolean mobActivates( Game game ) { return false; }
    };
    DiplomatNegotiateFealty.PERCENT_SEEN = 100;
    assertEquals( 22, playerB.getKnownItems().size() );
    order.execute( game );

    assertTrue( order.wasExecuted() );
    assertFalse( order.wasSuccessful() );
    assertEquals( playerB, popForB.getOwner() );
    assertTrue( playerB.getPopCenters().contains( popForB ) );
    assertEquals( 23, playerB.getKnownItems().size() );
    assertEquals( diplomatForA, playerB.getKnownItem( diplomatForA.getId() ) );
  }

  @Test
  public void testNegotiateFailsMobActivates()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Diplomat diplomatForA = playerA.getDiplomats().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    PopCenter popForB = game.getPopCenter( new Tuple( 1, 1 ) );
    playerB.add( popForB );
    diplomatForA.setBase( popForB );

    DiplomatNegotiateFealty order = new DiplomatNegotiateFealty( diplomatForA )
    {
      protected boolean makeAttempt( Game game ) { return false; }

      protected boolean mobActivates( Game game ) { return true; }
    };
    assertNotNull( playerA.getDiplomat( diplomatForA.getId() ) );
    DiplomatNegotiateFealty.PERCENT_SEEN = 100;
    order.execute( game );
    assertNull( playerA.getDiplomat( diplomatForA.getId() ) );
    assertEquals( 22, playerB.getKnownItems().size() );
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
    loyaltyOrder.execute( game );
    assertTrue( loyaltyOrder.wasExecuted() );
    assertEquals( 1, game.getCurrentTurn().getOrdersOfType( DiplomatInspireLoyalty.class ).size() );

    DiplomatNegotiateFealty fealtyOrder = new DiplomatNegotiateFealty( diplomatForA );
    assertEquals( 13, fealtyOrder.determineResistance( game ) );
  }

  @Test
  public void testEmbassyImpact()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Diplomat diplomatForA = playerA.getDiplomats().get( 0 );
    PopCenter popForB = game.getPopCenter( 1, 1 );
    playerB.add( popForB );
    popForB.setLevel( 2 );
    diplomatForA.setBase( popForB );
    Region region = game.getMap().getLocation( 1, 1 ).getRegion();

    // The default resistance.
    DiplomatNegotiateFealty fealtyOrder = new DiplomatNegotiateFealty( diplomatForA );
    assertEquals( 4, fealtyOrder.determineResistance( game ) );

    // Give playerA an embassy, see things got easier.
    playerA.improveEmbassy( region );
    assertEquals( 2, fealtyOrder.determineResistance( game ) );

    // Give playerB an embassy, see things got harder.
    playerB.improveEmbassy( region );
    playerB.improveEmbassy( region );
    assertEquals( 6, fealtyOrder.determineResistance( game ) );

    // Make the pop unowned, see it got easier again.
    playerB.remove( popForB );
    popForB.setOwner( Player.UNOWNED );
    assertEquals( 1, fealtyOrder.determineResistance( game ) );
  }
}

