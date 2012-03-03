package kessel.hex.orders.army;

import kessel.hex.domain.GameItem;
import kessel.hex.orders.AbstractOrderTest;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import static org.junit.Assert.*;

/** Test army transfers. */
public class AbstractArmyTransferTest<X extends ArmyTransfer > extends AbstractOrderTest<X>
{
  private static final Logger LOG = Logger.getLogger( AbstractArmyTransferTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  public void doEqualsTest( X a, X b ) throws Exception
  {
    super.doEqualsTest( a, b );
    assertEquals( a._target.getId(), b._jsonTargetId.intValue() );
    assertEquals( a._transfers.size(), b._jsonTransferIds.size() );
    for ( GameItem transfer : a._transfers )
    {
      assertTrue( b._jsonTransferIds.contains( transfer.getId() ) );
    }
  }
}

