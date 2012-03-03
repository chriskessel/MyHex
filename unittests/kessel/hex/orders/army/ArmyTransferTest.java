package kessel.hex.orders.army;

import kessel.hex.domain.Army;
import kessel.hex.domain.Diplomat;
import kessel.hex.domain.Game;
import kessel.hex.domain.GameItem;
import kessel.hex.domain.GameTest;
import kessel.hex.domain.Player;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/** Test army transfers. */
@SuppressWarnings({ "AssignmentToStaticFieldFromInstanceMethod" })
public class ArmyTransferTest extends AbstractArmyTransferTest<ArmyTransfer>
{
  private static final Logger LOG = Logger.getLogger( ArmyTransferTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  @Test
  public void testPersistence() throws Exception
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Army armyOneForA = playerA.getArmies().get( 0 );
    Army armyTwoForA = playerA.getArmies().get( 1 );
    List<GameItem> transfers = new ArrayList<>();
    transfers.add( armyOneForA.getUnits().get( 0 ) );
    transfers.add( armyOneForA.getUnits().get( 1 ) );

    ArmyTransfer oldOrder = new ArmyTransfer( armyOneForA, armyTwoForA, transfers );
    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    ArmyTransfer newOrder = Game.GSON.fromJson( jsonOrder, ArmyTransfer.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testSubjectInactive()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Army armyOneForA = playerA.getArmies().get( 0 );
    Army armyTwoForA = playerA.getArmies().get( 1 );
    armyOneForA.getUnits().clear();

    ArmyTransfer order = new ArmyTransfer( armyOneForA, armyTwoForA, new ArrayList<GameItem>() );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testTransfersInvalidNull()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Army armyOneForA = playerA.getArmies().get( 0 );
    Army armyTwoForA = playerA.getArmies().get( 1 );

    ArmyTransfer order = new ArmyTransfer( armyOneForA, armyTwoForA, null );
    order = Game.GSON.fromJson( Game.GSON.toJson( order ), ArmyTransfer.class );
    order.fixDeserializationReferences( game );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testTransfersInvalidEmpty()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Army armyOneForA = playerA.getArmies().get( 0 );
    Army armyTwoForA = playerA.getArmies().get( 1 );

    ArmyTransfer order = new ArmyTransfer( armyOneForA, armyTwoForA, new ArrayList<GameItem>() );
    order = Game.GSON.fromJson( Game.GSON.toJson( order ), ArmyTransfer.class );
    order.fixDeserializationReferences( game );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testTransfersInvalidItems()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Army armyOneForA = playerA.getArmies().get( 0 );
    Army armyTwoForA = playerA.getArmies().get( 1 );

    ArmyTransfer order = new ArmyTransfer( armyOneForA, armyTwoForA, Arrays.<GameItem>asList( playerA.getCapitol() ) );
    order = Game.GSON.fromJson( Game.GSON.toJson( order ), ArmyTransfer.class );
    order.fixDeserializationReferences( game );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testTransferTargetMissing()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Army armyOneForA = playerA.getArmies().get( 0 );
    Army armyTwoForA = new Army( game.generateUniqueId(), "foo", 0, playerA.getCapitol().getLocation() );

    ArmyTransfer order = new ArmyTransfer( armyOneForA, armyTwoForA, Arrays.<GameItem>asList( armyOneForA.getUnits().get( 0 ) ) );
    order = Game.GSON.fromJson( Game.GSON.toJson( order ), ArmyTransfer.class );
    order.fixDeserializationReferences( game );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testTransferTargetWrongLocation()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Army armyOneForA = playerA.getArmies().get( 0 );
    Army armyTwoForA = playerA.getArmies().get( 1 );
    armyTwoForA.setLocation( playerB.getCapitol().getLocation() );

    ArmyTransfer order = new ArmyTransfer( armyOneForA, armyTwoForA, Arrays.<GameItem>asList( armyOneForA.getUnits().get( 0 ) ) );
    order = Game.GSON.fromJson( Game.GSON.toJson( order ), ArmyTransfer.class );
    order.fixDeserializationReferences( game );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testTransferWorksBothStayActive()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Army armyOneForA = playerA.getArmies().get( 0 );
    Army armyTwoForA = playerA.getArmies().get( 1 );
    armyTwoForA.setLocation( armyOneForA.getLocation() );

    ArmyTransfer order = new ArmyTransfer( armyOneForA, armyTwoForA,
      Arrays.<GameItem>asList( armyOneForA.getUnits().get( 0 ), armyOneForA.getUnits().get( 1 ) ) );
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertEquals( 2, armyOneForA.getUnits().size() );
    assertEquals( 4, armyTwoForA.getUnits().size() );
  }

  @Test
  public void testTransferWorksSourceGoesInactive()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Army armyOneForA = playerA.getArmies().get( 0 );
    Army armyTwoForA = playerA.getArmies().get( 1 );
    armyTwoForA.setLocation( armyOneForA.getLocation() );
    Diplomat diplomat = playerA.getDiplomats().get( 0 );
    diplomat.setBase( armyOneForA );

    ArmyTransfer order = new ArmyTransfer( armyOneForA, armyTwoForA, armyOneForA.getUnits() );

    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertEquals( 0, armyOneForA.getUnits().size() );
    assertEquals( 6, armyTwoForA.getUnits().size() );
    assertFalse( armyOneForA.isActive() );
    assertEquals( diplomat.getBase(), armyTwoForA );
  }
}
