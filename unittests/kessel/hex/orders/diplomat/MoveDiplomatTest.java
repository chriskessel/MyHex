package kessel.hex.orders.diplomat;

import kessel.hex.domain.Army;
import kessel.hex.domain.ArmyUnit;
import kessel.hex.domain.Diplomat;
import kessel.hex.domain.Game;
import kessel.hex.domain.GameTest;
import kessel.hex.domain.Player;
import kessel.hex.domain.PopCenter;
import kessel.hex.domain.TroopType;
import kessel.hex.orders.AbstractMoveFigureTest;
import kessel.hex.util.Tuple;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import static org.junit.Assert.*;

/** Test moving diplomats. */
@SuppressWarnings({ "AssignmentToStaticFieldFromInstanceMethod" })
public class MoveDiplomatTest extends AbstractMoveFigureTest<MoveDiplomat>
{
  private static final Logger LOG = Logger.getLogger( MoveDiplomatTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  @Test
  public void testPersistence() throws Exception
  {
    Game game = GameTest.createSimpleGame();
    PopCenter oldPopCenter = game.getPopCenter( new Tuple( 0, 0 ) );
    Diplomat diplomat = new Diplomat( game.generateUniqueId(), "Bob", 3, oldPopCenter );
    Player playerA = game.getPlayers().get( 0 );
    playerA.add( diplomat );
    MoveDiplomat oldOrder = new MoveDiplomat( diplomat, oldPopCenter );

    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    MoveDiplomat newOrder = Game.GSON.fromJson( jsonOrder, MoveDiplomat.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testMoveToPopCenter()
  {
    Game game = GameTest.createSimpleGame();
    game.setTurn( 4 );
    PopCenter oldPopCenter = game.getPopCenter( new Tuple( 0, 0 ) );
    Diplomat diplomat = new Diplomat( game.generateUniqueId(), "Bob", 3, oldPopCenter );
    assertSame( oldPopCenter, diplomat.getBase() );
    Player playerA = game.getPlayers().get( 0 );
    playerA.add( diplomat );

    // Move the Diplomat to a new pop center.
    PopCenter newPopCenter = game.getPopCenter( new Tuple( 1, 1 ) );
    assertNotSame( oldPopCenter, newPopCenter );
    MoveDiplomat order = new MoveDiplomat( diplomat, newPopCenter );
    order = Game.GSON.fromJson( Game.GSON.toJson( order ), MoveDiplomat.class );
    order.fixDeserializationReferences( game );
    order.execute( game );

    assertSame( diplomat, playerA.getDiplomat( diplomat.getId() ) );
    assertSame( newPopCenter, diplomat.getBase() );
    assertEquals( playerA.getKingdom().getStartingDiplomats().size() + 1, playerA.getDiplomats().size() );
  }

  @Test
  public void testMoveToMyArmy()
  {
    Game game = GameTest.createSimpleGame();
    game.setTurn( 4 );
    PopCenter oldPopCenter = game.getPopCenter( new Tuple( 0, 0 ) );
    Diplomat diplomat = new Diplomat( game.generateUniqueId(), "Bob", 3, oldPopCenter );
    assertSame( oldPopCenter, diplomat.getBase() );
    Player playerA = game.getPlayers().get( 0 );
    playerA.add( diplomat );
    Army army = new Army( game.generateUniqueId(), "ArmyA", 3, game.getMap().getLocation( 1, 1 ) );
    army.addUnit( new ArmyUnit( TroopType.LEVY ) );
    playerA.add( army );

    // Move the Diplomat to army;
    MoveDiplomat order = new MoveDiplomat( diplomat, army );
    order = Game.GSON.fromJson( Game.GSON.toJson( order ), MoveDiplomat.class );
    order.fixDeserializationReferences( game );
    order.execute( game );

    assertSame( diplomat, playerA.getDiplomat( diplomat.getId() ) );
    assertSame( army, diplomat.getBase() );
  }

  @Test
  public void testMoveToNotMyArmy()
  {
    Game game = GameTest.createSimpleGame();
    game.setTurn( 4 );
    PopCenter oldPopCenter = game.getPopCenter( new Tuple( 0, 0 ) );
    Diplomat diplomat = new Diplomat( game.generateUniqueId(), "Bob", 3, oldPopCenter );
    assertSame( oldPopCenter, diplomat.getBase() );
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    playerA.add( diplomat );
    Army army = new Army( game.generateUniqueId(), "ArmyB", 3, game.getMap().getLocation( 1, 1 ) );
    playerB.add( army );

    // Move the Diplomat to another player's army, which is invalid and should do nothing.
    MoveDiplomat order = new MoveDiplomat( diplomat, army );
    assertEquals( 0, playerA.getGameEvents().size() );
    order = Game.GSON.fromJson( Game.GSON.toJson( order ), MoveDiplomat.class );
    order.fixDeserializationReferences( game );
    order.execute( game );

    assertEquals( 1, playerA.getGameEvents().size() );
    assertSame( diplomat, playerA.getDiplomat( diplomat.getId() ) );
    assertSame( oldPopCenter, diplomat.getBase() );
  }

  @Test
  public void testMoveOutOfRange()
  {
    Game game = GameTest.createSimpleGame();
    game.setTurn( 4 );
    PopCenter oldPopCenter = game.getPopCenter( new Tuple( 0, 0 ) );
    Diplomat diplomat = new Diplomat( game.generateUniqueId(), "Bob", 3, oldPopCenter );
    assertSame( oldPopCenter, diplomat.getBase() );
    Player playerA = game.getPlayers().get( 0 );
    playerA.add( diplomat );

    // Move the Diplomat to a new pop center.
    PopCenter newPopCenter = game.getPopCenter( new Tuple( 1, 1 ) );
    assertNotSame( oldPopCenter, newPopCenter );
    MoveDiplomat order = new MoveDiplomat( diplomat, newPopCenter );
    diplomat.setRange( 0 );
    order = Game.GSON.fromJson( Game.GSON.toJson( order ), MoveDiplomat.class );
    order.fixDeserializationReferences( game );
    order.execute( game );

    // The move should fail as it's out of range.
    assertSame( diplomat, playerA.getDiplomat( diplomat.getId() ) );
    assertSame( oldPopCenter, diplomat.getBase() );

    // The move should now work as it's in range.
    diplomat.setRange( 2 );
    order.execute( game );
    assertSame( newPopCenter, diplomat.getBase() );
  }

  @Test
  public void testMoveWasSeen()
  {
    Game game = GameTest.createSimpleGame();
    game.setTurn( 4 );
    PopCenter oldPopCenter = game.getPopCenter( new Tuple( 0, 0 ) );
    Diplomat diplomat = new Diplomat( game.generateUniqueId(), "Bob", 3, oldPopCenter );
    Player playerA = game.getPlayers().get( 0 );
    playerA.add( diplomat );

    // Move the Diplomat to a new pop center.
    Player playerB = game.getPlayers().get( 1 );
    PopCenter newPopCenter = game.getPopCenter( new Tuple( 1, 1 ) );
    newPopCenter.setOwner( playerB );
    MoveDiplomat order = new MoveDiplomat( diplomat, newPopCenter );
    assertEquals( 22, playerB.getKnownItems().size() );
    order = Game.GSON.fromJson( Game.GSON.toJson( order ), MoveDiplomat.class );
    order.fixDeserializationReferences( game );
    order._percentSeen = 100;
    order.execute( game );

    // Validate the move was seen and the seen info is appropriately vague.
    assertSame( newPopCenter, diplomat.getBase() );
    assertEquals( 23, playerB.getKnownItems().size() );
    Diplomat seenMove = (Diplomat) playerB.getKnownItems().get( 22 );
    assertEquals( 4, seenMove.getTurnSeen() );
    assertEquals( "Unknown", seenMove.getName() );
    assertEquals( Player.UNOWNED, seenMove.getOwner() );
    assertEquals( newPopCenter, seenMove.getBase() );
  }

  @Test
  public void testMoveWasSeenButPopUnowned()
  {
    Game game = GameTest.createSimpleGame();
    game.setTurn( 4 );
    PopCenter oldPopCenter = game.getPopCenter( new Tuple( 0, 0 ) );
    Player playerA = game.getPlayers().get( 0 );
    Diplomat diplomat = playerA.getDiplomats().get( 0 );
    diplomat.setBase( oldPopCenter );

    // Move the Diplomat to a new pop center.
    Player playerB = game.getPlayers().get( 1 );
    PopCenter newPopCenter = game.getPopCenter( new Tuple( 1, 1 ) );
    assertEquals( Player.UNOWNED, newPopCenter.getOwner() );
    MoveDiplomat order = new MoveDiplomat( diplomat, newPopCenter );
    assertEquals( 22, playerB.getKnownItems().size() );
    order = Game.GSON.fromJson( Game.GSON.toJson( order ), MoveDiplomat.class );
    order.fixDeserializationReferences( game );
    order._percentSeen = 100;
    order.execute( game );

    // Validate the move doesn't show up in anyone's view.
    assertSame( newPopCenter, diplomat.getBase() );
    assertEquals( 24, playerA.getKnownItems().size() );
    assertEquals( 22, playerB.getKnownItems().size() );
  }

  @Test
  public void testMoveWasSeenButIsMyPop()
  {
    Game game = GameTest.createSimpleGame();
    game.setTurn( 4 );
    PopCenter oldPopCenter = game.getPopCenter( new Tuple( 0, 0 ) );
    Diplomat diplomat = new Diplomat( game.generateUniqueId(), "Bob", 3, oldPopCenter );
    Player playerA = game.getPlayers().get( 0 );
    playerA.add( diplomat );

    // Move the Diplomat to a new pop center.
    Player playerB = game.getPlayers().get( 1 );
    PopCenter newPopCenter = game.getPopCenter( new Tuple( 1, 1 ) );
    newPopCenter.setOwner( playerA );
    assertEquals( playerA, newPopCenter.getOwner() );
    MoveDiplomat order = new MoveDiplomat( diplomat, newPopCenter );
    assertEquals( 22, playerB.getKnownItems().size() );
    order = Game.GSON.fromJson( Game.GSON.toJson( order ), MoveDiplomat.class );
    order.fixDeserializationReferences( game );
    order._percentSeen = 100;
    order.execute( game );

    // Validate the move doesn't show up in anyone's view.
    assertSame( newPopCenter, diplomat.getBase() );
    assertEquals( 25, playerA.getKnownItems().size() );
    assertEquals( 22, playerB.getKnownItems().size() );
  }

  @Test
  public void testMoveWasUnseen()
  {
    Game game = GameTest.createSimpleGame();
    game.setTurn( 4 );
    PopCenter oldPopCenter = game.getPopCenter( new Tuple( 0, 0 ) );
    Diplomat diplomat = new Diplomat( game.generateUniqueId(), "Bob", 3, oldPopCenter );
    Player playerA = game.getPlayers().get( 0 );
    playerA.add( diplomat );

    // Move the Diplomat to a new pop center.
    Player playerB = game.getPlayers().get( 1 );
    PopCenter newPopCenter = game.getPopCenter( new Tuple( 1, 1 ) );
    newPopCenter.setOwner( playerB );
    MoveDiplomat order = new MoveDiplomat( diplomat, newPopCenter );
    assertEquals( 22, playerB.getKnownItems().size() );
    order = Game.GSON.fromJson( Game.GSON.toJson( order ), MoveDiplomat.class );
    order.fixDeserializationReferences( game );
    order._percentSeen = 0;
    order.execute( game );

    // Validate the move was not seen by playerB.
    assertSame( newPopCenter, diplomat.getBase() );
    assertEquals( 22, playerB.getKnownItems().size() );
  }

  @Test
  public void testMoveWasBlocked()
  {
    Game game = GameTest.createSimpleGame();
    PopCenter oldPopCenter = game.getPopCenter( new Tuple( 0, 0 ) );
    Diplomat diplomat = new Diplomat( game.generateUniqueId(), "Bob", 3, oldPopCenter );
    Player playerA = game.getPlayers().get( 0 );
    playerA.add( diplomat );

    // Move to a pop with an opposing army.
    Player playerB = game.getPlayers().get( 1 );
    PopCenter newPopCenter = playerB.getCapitol();
    MoveDiplomat order = new MoveDiplomat( diplomat, newPopCenter );
    order = Game.GSON.fromJson( Game.GSON.toJson( order ), MoveDiplomat.class );
    order.fixDeserializationReferences( game );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }
}
