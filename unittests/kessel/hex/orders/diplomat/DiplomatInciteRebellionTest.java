package kessel.hex.orders.diplomat;

import kessel.hex.domain.Army;
import kessel.hex.domain.Diplomat;
import kessel.hex.domain.Game;
import kessel.hex.domain.GameTest;
import kessel.hex.domain.Player;
import kessel.hex.domain.PopCenter;
import kessel.hex.orders.AbstractOrderTest;
import kessel.hex.util.Tuple;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import static org.junit.Assert.*;

/** Test removing control of pops. */
@SuppressWarnings({ "AssignmentToStaticFieldFromInstanceMethod" })
public class DiplomatInciteRebellionTest extends AbstractOrderTest<DiplomatInciteRebellion>
{
  private static final Logger LOG = Logger.getLogger( DiplomatInciteRebellionTest.class );

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

    DiplomatInciteRebellion oldOrder = new DiplomatInciteRebellion( diplomatForA );
    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    DiplomatInciteRebellion newOrder = Game.GSON.fromJson( jsonOrder, DiplomatInciteRebellion.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testIsNotInPop()
  {
    Game game = GameTest.createSimpleGame();
    Diplomat diplomatForA = game.getPlayers().get( 0 ).getDiplomats().get( 0 );
    Army armyForA = game.getPlayers().get( 0 ).getArmies().get( 0 );
    diplomatForA.setBase( armyForA );

    DiplomatInciteRebellion order = new DiplomatInciteRebellion( diplomatForA );
    order = Game.GSON.fromJson( Game.GSON.toJson( order ), DiplomatInciteRebellion.class );
    order.fixDeserializationReferences( game );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testPopIsNotOwned()
  {
    Game game = GameTest.createSimpleGame();
    Diplomat diplomatForA = game.getPlayers().get( 0 ).getDiplomats().get( 0 );
    PopCenter unownedPop = game.getPopCenter( new Tuple( 1, 1 ) );
    diplomatForA.setBase( unownedPop );
    assertEquals( Player.UNOWNED, unownedPop.getOwner() );

    DiplomatInciteRebellion order = new DiplomatInciteRebellion( diplomatForA );
    order = Game.GSON.fromJson( Game.GSON.toJson( order ), DiplomatInciteRebellion.class );
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

    DiplomatInciteRebellion order = new DiplomatInciteRebellion( diplomatForA );
    order = Game.GSON.fromJson( Game.GSON.toJson( order ), DiplomatInciteRebellion.class );
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

    DiplomatInciteRebellion order = new DiplomatInciteRebellion( diplomatForA );
    order = Game.GSON.fromJson( Game.GSON.toJson( order ), DiplomatInciteRebellion.class );
    order.fixDeserializationReferences( game );
    diplomatForA.addOrderExecuted( order );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testDiplomatInciteInOwnCapital()
  {
    Game game = GameTest.createSimpleGame();
    Diplomat diplomatForA = game.getPlayers().get( 0 ).getDiplomats().get( 0 );
    PopCenter capitolForA = game.getPlayers().get( 0 ).getCapitol();
    diplomatForA.setBase( capitolForA );

    DiplomatInciteRebellion order = new DiplomatInciteRebellion( diplomatForA );
    order = Game.GSON.fromJson( Game.GSON.toJson( order ), DiplomatInciteRebellion.class );
    order.fixDeserializationReferences( game );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testIncitingInOwnNonCapitol()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Diplomat diplomatForA = playerA.getDiplomats().get( 0 );
    PopCenter nonCapitalPopForA = game.getPopCenter( new Tuple( 1, 1 ) );
    playerA.add( nonCapitalPopForA );
    diplomatForA.setBase( nonCapitalPopForA );

    diplomatForA.setLevel( 0 ); // Level zero always fails, except for this case which should work.
    DiplomatInciteRebellion order = new DiplomatInciteRebellion( diplomatForA );
    order = Game.GSON.fromJson( Game.GSON.toJson( order ), DiplomatInciteRebellion.class );
    order.fixDeserializationReferences( game );
    assertEquals( 24, playerA.getKnownItems().size() );
    order.execute( game );

    assertTrue( order.wasExecuted() );
    assertTrue( order.wasSuccessful() );
    assertFalse( playerA.getPopCenters().contains( nonCapitalPopForA ) );
    assertEquals( Player.UNOWNED, nonCapitalPopForA.getOwner() );
    assertEquals( 25, playerA.getKnownItems().size() );
    assertEquals( nonCapitalPopForA, playerA.getKnownPopCenter( nonCapitalPopForA.getId() ) );
  }

  @Test
  public void testIncitingOtherCapitol()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Diplomat diplomatForA = playerA.getDiplomats().get( 0 );
    PopCenter capitolForB = playerB.getCapitol();
    diplomatForA.setBase( capitolForB );
    playerA.setGold( 100000 );

    diplomatForA.setLevel( 100 );
    DiplomatInciteRebellion order = new DiplomatInciteRebellion( diplomatForA );
    order = Game.GSON.fromJson( Game.GSON.toJson( order ), DiplomatInciteRebellion.class );
    order.fixDeserializationReferences( game );
    order.execute( game );

    assertTrue( order.wasExecuted() );
    assertFalse( order.wasSuccessful() );
  }

  @Test
  public void testInciteSucceedsNotSeen()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Diplomat diplomatForA = playerA.getDiplomats().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    PopCenter popForB = game.getPopCenter( new Tuple( 1, 1 ) );
    playerB.add( popForB );
    diplomatForA.setBase( popForB );

    DiplomatInciteRebellion order = new DiplomatInciteRebellion( diplomatForA )
    {
      protected boolean makeAttempt( Game game ) { return true; }
    };
    DiplomatInciteRebellion.PERCENT_SEEN = 0;
    assertEquals( 22, playerB.getKnownItems().size() );
    order.execute( game );

    assertTrue( order.wasExecuted() );
    assertTrue( order.wasSuccessful() );
    assertFalse( playerB.getPopCenters().contains( popForB ) );
    assertEquals( Player.UNOWNED, popForB.getOwner() );
    assertEquals( 23, playerB.getKnownItems().size() );
    assertEquals( popForB, playerB.getKnownPopCenter( popForB.getId() ) );
  }

  @Test
  public void testInciteSucceedsIsSeen()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Diplomat diplomatForA = playerA.getDiplomats().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    PopCenter popForB = game.getPopCenter( new Tuple( 1, 1 ) );
    playerB.add( popForB );
    diplomatForA.setBase( popForB );

    DiplomatInciteRebellion order = new DiplomatInciteRebellion( diplomatForA )
    {
      protected boolean makeAttempt( Game game ) { return true; }
    };
    DiplomatInciteRebellion.PERCENT_SEEN = 100;
    assertEquals( 22, playerB.getKnownItems().size() );
    order.execute( game );

    assertTrue( order.wasExecuted() );
    assertTrue( order.wasSuccessful() );
    assertFalse( playerB.getPopCenters().contains( popForB ) );
    assertEquals( Player.UNOWNED, popForB.getOwner() );
    assertEquals( 24, playerB.getKnownItems().size() );
    assertEquals( diplomatForA, playerB.getKnownItem( diplomatForA.getId() ) );
    assertEquals( popForB, playerB.getKnownPopCenter( popForB.getId() ) );
  }

  @Test
  public void testInciteFailsNotSeen()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Diplomat diplomatForA = playerA.getDiplomats().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    PopCenter popForB = game.getPopCenter( new Tuple( 1, 1 ) );
    playerB.add( popForB );
    diplomatForA.setBase( popForB );

    DiplomatInciteRebellion order = new DiplomatInciteRebellion( diplomatForA )
    {
      protected boolean makeAttempt( Game game ) { return false; }
    };
    DiplomatInciteRebellion.PERCENT_SEEN = 0;
    assertEquals( 22, playerB.getKnownItems().size() );
    order.execute( game );

    assertTrue( order.wasExecuted() );
    assertFalse( order.wasSuccessful() );
    assertEquals( playerB, popForB.getOwner() );
    assertTrue( playerB.getPopCenters().contains( popForB ) );
    assertEquals( 22, playerB.getKnownItems().size() );
  }

  @Test
  public void testInciteFailsIsSeen()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Diplomat diplomatForA = playerA.getDiplomats().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    PopCenter popForB = game.getPopCenter( new Tuple( 1, 1 ) );
    playerB.add( popForB );
    diplomatForA.setBase( popForB );

    DiplomatInciteRebellion order = new DiplomatInciteRebellion( diplomatForA )
    {
      protected boolean makeAttempt( Game game ) { return false; }

      protected boolean mobActivates( Game game ) { return false; }
    };
    DiplomatInciteRebellion.PERCENT_SEEN = 100;
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
  public void testInciteFailsMobActivates()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Diplomat diplomatForA = playerA.getDiplomats().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    PopCenter popForB = game.getPopCenter( new Tuple( 1, 1 ) );
    playerB.add( popForB );
    diplomatForA.setBase( popForB );

    DiplomatInciteRebellion order = new DiplomatInciteRebellion( diplomatForA )
    {
      protected boolean makeAttempt( Game game ) { return false; }

      protected boolean mobActivates( Game game ) { return true; }
    };
    assertNotNull( playerA.getDiplomat( diplomatForA.getId() ) );
    DiplomatInciteRebellion.PERCENT_SEEN = 100;
    order.execute( game );
    assertNull( playerA.getDiplomat( diplomatForA.getId() ) );
    assertEquals( 22, playerB.getKnownItems().size() );
  }
}

