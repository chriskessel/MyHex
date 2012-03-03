package kessel.hex.orders.wizard;

import kessel.hex.orders.AbstractOrderTest;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class AbstractInvisibleArmyTest<X extends AbstractInvisibleArmy> extends AbstractOrderTest<X>
{
  private static final Logger LOG = Logger.getLogger( AbstractInvisibleArmyTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }
}
