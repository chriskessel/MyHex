package kessel.hex.orders.agent;

import kessel.hex.domain.Agent;
import kessel.hex.domain.Army;
import kessel.hex.domain.ArmyUnit;
import kessel.hex.domain.Game;
import kessel.hex.domain.GameTest;
import kessel.hex.domain.Player;
import kessel.hex.domain.PopCenter;
import kessel.hex.domain.TroopType;
import kessel.hex.domain.Wizard;
import kessel.hex.orders.AbstractMoveFigureTest;
import kessel.hex.orders.wizard.CharmFigure;
import kessel.hex.util.Tuple;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import static org.junit.Assert.*;

/** Test moving agents. */
@SuppressWarnings({ "AssignmentToStaticFieldFromInstanceMethod" })
public class MoveAgentTest extends AbstractMoveFigureTest<MoveAgent>
{
  private static final Logger LOG = Logger.getLogger( MoveAgentTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  @Test
  public void testPersistence() throws Exception
  {
    Game game = GameTest.createSimpleGame();
    PopCenter oldPopCenter = game.getPopCenter( new Tuple( 0, 0 ) );
    Agent agent = new Agent( game.generateUniqueId(), "Bob", 3, oldPopCenter );
    Player playerA = game.getPlayers().get( 0 );
    playerA.add( agent );

    MoveAgent oldOrder = new MoveAgent( agent, oldPopCenter );
    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    MoveAgent newOrder = Game.GSON.fromJson( jsonOrder, MoveAgent.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testCharmed()
  {
    // Execute the charm.
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Wizard wizardForB = playerB.getWizards().get( 0 );
    wizardForB.setLevel( 2 );
    Agent agentForA = playerA.getAgents().get( 0 );

    CharmFigure charmOrder = new CharmFigure( wizardForB, agentForA );
    charmOrder.execute( game );

    assertTrue( charmOrder.wasExecuted() );
    assertEquals( 1, game.getCurrentTurn().getOrdersOfType( CharmFigure.class ).size() );

    // Now try the move, it should fail due to the charm.
    PopCenter pop = game.getPopCenter( new Tuple( 1, 0 ) );
    MoveAgent order = new MoveAgent( agentForA, pop );
    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testMoveToPopCenter()
  {
    Game game = GameTest.createSimpleGame();
    game.setTurn( 4 );
    PopCenter oldPopCenter = game.getPopCenter( new Tuple( 0, 0 ) );
    Agent agent = new Agent( game.generateUniqueId(), "Bob", 3, oldPopCenter );
    assertSame( oldPopCenter, agent.getBase() );
    Player playerA = game.getPlayers().get( 0 );
    playerA.add( agent );

    // Move the Agent to a new pop center.
    PopCenter newPopCenter = game.getPopCenter( new Tuple( 1, 0 ) );
    assertNotSame( oldPopCenter, newPopCenter );
    MoveAgent order = new MoveAgent( agent, newPopCenter );
    order.execute( game );

    assertSame( agent, playerA.getAgent( agent.getId() ) );
    assertSame( newPopCenter, agent.getBase() );
    assertEquals( playerA.getKingdom().getStartingAgents().size() + 1, playerA.getAgents().size() );
  }

  @Test
  public void testMoveToMyArmy()
  {
    Game game = GameTest.createSimpleGame();
    game.setTurn( 4 );
    PopCenter oldPopCenter = game.getPopCenter( new Tuple( 0, 0 ) );
    Agent agent = new Agent( game.generateUniqueId(), "Bob", 3, oldPopCenter );
    assertSame( oldPopCenter, agent.getBase() );
    Player playerA = game.getPlayers().get( 0 );
    playerA.add( agent );
    Army army = new Army( game.generateUniqueId(), "ArmyA", 3, game.getMap().getLocation( 1, 1 ) );
    army.addUnit( new ArmyUnit( TroopType.LEVY ) );
    playerA.add( army );

    // Move the Agent to army.
    MoveAgent order = new MoveAgent( agent, army );
    order.execute( game );

    assertSame( agent, playerA.getAgent( agent.getId() ) );
    assertSame( army, agent.getBase() );
  }

  @Test
  public void testMoveToMyInactiveArmy()
  {
    Game game = GameTest.createSimpleGame();
    game.setTurn( 4 );
    PopCenter oldPopCenter = game.getPopCenter( new Tuple( 0, 0 ) );
    Agent agent = new Agent( game.generateUniqueId(), "Bob", 3, oldPopCenter );
    assertSame( oldPopCenter, agent.getBase() );
    Player playerA = game.getPlayers().get( 0 );
    playerA.add( agent );
    Army army = new Army( game.generateUniqueId(), "ArmyA", 3, game.getMap().getLocation( 1, 1 ) );
    playerA.add( army );
    army.getUnits().clear();

    // Move the Agent to army.
    MoveAgent order = new MoveAgent( agent, army );
    order.execute( game );

    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testMoveToNotMyArmy()
  {
    Game game = GameTest.createSimpleGame();
    game.setTurn( 4 );
    PopCenter oldPopCenter = game.getPopCenter( new Tuple( 0, 0 ) );
    Agent agent = new Agent( game.generateUniqueId(), "Bob", 3, oldPopCenter );
    assertSame( oldPopCenter, agent.getBase() );
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    playerA.add( agent );
    Army army = new Army( game.generateUniqueId(), "ArmyB", 3, game.getMap().getLocation( 1, 1 ) );
    playerB.add( army );

    // Move the Agent to another player's army, which is invalid and should do nothing.
    MoveAgent order = new MoveAgent( agent, army );
    order.execute( game );

    assertSame( agent, playerA.getAgent( agent.getId() ) );
    assertSame( oldPopCenter, agent.getBase() );
  }

  @Test
  public void testMoveOutOfRange()
  {
    Game game = GameTest.createSimpleGame();
    game.setTurn( 4 );
    PopCenter oldPopCenter = game.getPopCenter( new Tuple( 0, 0 ) );
    Agent agent = new Agent( game.generateUniqueId(), "Bob", 3, oldPopCenter );
    assertSame( oldPopCenter, agent.getBase() );
    Player playerA = game.getPlayers().get( 0 );
    playerA.add( agent );

    // Move the Agent to a new pop center.
    PopCenter newPopCenter = game.getPopCenter( new Tuple( 1, 0 ) );
    assertNotSame( oldPopCenter, newPopCenter );
    MoveAgent order = new MoveAgent( agent, newPopCenter );
    agent.setRange( 0 );
    order.execute( game );

    // The move should fail as it's out of range.
    assertSame( agent, playerA.getAgent( agent.getId() ) );
    assertSame( oldPopCenter, agent.getBase() );

    // The move should now work as it's in range.
    agent.setRange( 1 );
    order.execute( game );
    assertSame( newPopCenter, agent.getBase() );
  }
}
