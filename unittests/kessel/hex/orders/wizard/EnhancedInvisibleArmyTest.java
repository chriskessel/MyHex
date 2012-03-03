package kessel.hex.orders.wizard;

import kessel.hex.domain.Army;
import kessel.hex.domain.ArmyUnit;
import kessel.hex.domain.Game;
import kessel.hex.domain.GameTest;
import kessel.hex.domain.Player;
import kessel.hex.domain.TroopType;
import kessel.hex.domain.Wizard;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Test;

import static org.junit.Assert.*;

public class EnhancedInvisibleArmyTest extends AbstractInvisibleArmyTest<EnhancedInvisibleArmy>
{
  private static final Logger LOG = Logger.getLogger( EnhancedInvisibleArmyTest.class );

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

    EnhancedInvisibleArmy oldOrder = new EnhancedInvisibleArmy( wizard );
    String jsonOrder = Game.GSON.toJson( oldOrder );
    LOG.debug( jsonOrder );
    EnhancedInvisibleArmy newOrder = Game.GSON.fromJson( jsonOrder, EnhancedInvisibleArmy.class );
    doEqualsTest( oldOrder, newOrder );
  }

  @Test
  public void testArmyTooBig()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Wizard wizard = playerA.getWizards().get( 0 );
    Army army = playerA.getActiveArmies().get( 0 );
    wizard.setBase( army );
    wizard.setLevel( 6 );
    while ( army.getUnits().size() < 13 )
    {
      army.addUnit( new ArmyUnit( TroopType.LEVY ) );
    }
    EnhancedInvisibleArmy order = new EnhancedInvisibleArmy( wizard );

    order.execute( game );
    assertFalse( order.wasExecuted() );
  }

  @Test
  public void testInvisible()
  {
    Game game = GameTest.createSimpleGame();
    Player playerA = game.getPlayers().get( 0 );
    Wizard wizard = playerA.getWizards().get( 0 );
    Army army = playerA.getActiveArmies().get( 0 );
    wizard.setBase( army );
    wizard.setLevel( 6 );
    EnhancedInvisibleArmy order = new EnhancedInvisibleArmy( wizard );

    assertFalse( army.isInvisible() );
    order.execute( game );
    assertTrue( order.wasExecuted() );
    assertTrue( army.isInvisible() );
  }
}
