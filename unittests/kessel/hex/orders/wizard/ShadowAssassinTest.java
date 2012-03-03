package kessel.hex.orders.wizard;

import kessel.hex.domain.Diplomat;
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

public class ShadowAssassinTest extends AbstractOrderTest<ShadowAssassin>
{
  private static final Logger LOG = Logger.getLogger( ShadowAssassinTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  public void doEqualsTest( ShadowAssassin a, ShadowAssassin b ) throws Exception
  {
    super.doEqualsTest( a, b );
    assertEquals( a.getTarget().getId(), b._jsonTargetId.intValue() );
    assertEquals( a._targetBase.getId(), b._jsonTargetBaseId.intValue() );
  }

  @Test
  public void testPersistence() throws Exception
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Wizard wizardForA = playerA.getWizards().get( 0 );
    Diplomat diplomatForB = playerB.getDiplomats().get( 0 );
    ShadowAssassin oldOrder = new ShadowAssassin( wizardForA, diplomatForB, diplomatForB.getBase() );

    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    ShadowAssassin newOrder = Game.GSON.fromJson( jsonOrder, ShadowAssassin.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testTargetShielded()
  {
    // Cast the shield.
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Wizard wizardForA = playerA.getWizards().get( 0 );
    Wizard wizardForB = playerB.getWizards().get( 0 );
    Diplomat diplomatForB = playerB.getDiplomats().get( 0 );
    wizardForB.setLevel( 2 );

    ShieldFigure shieldOrder = new ShieldFigure( wizardForB, diplomatForB );
    shieldOrder.execute( game );
    assertTrue( shieldOrder.wasExecuted() );
    assertEquals( 1, game.getCurrentTurn().getOrdersOfType( ShieldFigure.class ).size() );

    // Cast the assassin, it should fail due to the protection.
    ShadowAssassin assassinOrder = new ShadowAssassin( wizardForA, diplomatForB, diplomatForB.getBase() );
    assassinOrder.execute( game );
    assertFalse( assassinOrder.wasExecuted() );
  }

  @Test
  public void testTargetNotAtBase()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Wizard wizardForA = playerA.getWizards().get( 0 );
    Diplomat diplomatForB = playerB.getDiplomats().get( 0 );

    ShadowAssassin assassinOrder = new ShadowAssassin( wizardForA, diplomatForB, wizardForA.getBase() );
    assassinOrder.execute( game );
    assertFalse( assassinOrder.wasExecuted() );
  }

  @Test
  public void testSuccess()
  {
    ShadowAssassin order = executeShadowAgentMission( 1 );
    assertTrue( order.wasExecuted() );
    assertTrue( order.wasSuccessful() );
  }

  @Test
  public void testFail()
  {
    ShadowAssassin order = executeShadowAgentMission( 99 );
    assertTrue( order.wasExecuted() );
    assertFalse( order.wasSuccessful() );
  }

  private ShadowAssassin executeShadowAgentMission( int level )
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Player playerB = game.getPlayers().get( 1 );
    Wizard wizardForA = playerA.getWizards().get( 0 );
    Diplomat diplomatForB = playerB.getDiplomats().get( 0 );
    diplomatForB.setLevel( level );
    wizardForA.setLevel( 7 );

    ShadowAssassin order = new ShadowAssassin( wizardForA, diplomatForB, diplomatForB.getBase() );
    order.execute( game );
    return order;
  }
}
