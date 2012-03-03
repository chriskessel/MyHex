package kessel.hex.admin;

import kessel.hex.domain.GameItem;
import kessel.hex.util.Tuple;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/** Displays contextually relevant quick info, such as information about the hex the mouse is hovering over. */
class StatusPane extends JTextArea implements MouseInputListener
{
  private final AbstractGameDisplay _gameDisplay;

  StatusPane( AbstractGameDisplay gameDisplay )
  {
    super();
    setBorder( BorderFactory.createEtchedBorder() );
    _gameDisplay = gameDisplay;
    _gameDisplay._mapPane.addMouseMotionListener( this );
    setLineWrap( true );
    setText( "Please load a game" );
    setWrapStyleWord( true );
    setRows( 3 );
  }

  public void init()
  {
    //
  }

  public void mouseClicked( MouseEvent e )
  {
  }

  public void mousePressed( MouseEvent e )
  {
  }

  public void mouseReleased( MouseEvent e )
  {
  }

  public void mouseEntered( MouseEvent e )
  {
  }

  public void mouseExited( MouseEvent e )
  {
  }

  public void mouseDragged( MouseEvent e )
  {
  }

  public void mouseMoved( MouseEvent e )
  {
    if ( _gameDisplay.isLoaded() )
    {
      Tuple grid = AbstractGameDisplay.CELL_METRICS.getGridByPixel( e.getX(), e.getY() );
      if ( _gameDisplay.isOnMap( grid ) )
      {
        StringBuilder buf = new StringBuilder();
        List<GameItem> itemsAtHex = _gameDisplay.getItemsAtHex( grid );
        Collections.sort( itemsAtHex, AbstractDetailsPane.ItemComparator.INSTANCE );
        for ( Iterator<GameItem> iterator = itemsAtHex.iterator(); iterator.hasNext(); )
        {
          GameItem item = iterator.next();
          buf.append( item.getShortStatusName() );
          if ( iterator.hasNext() ) { buf.append( ", " ); }
        }
        String region = _gameDisplay.getLocationAtHex( grid ).getRegion().getName();
        setText( region + "-" + grid + ": " + buf );
      }
      else
      {
        setText( "" );
      }
    }
  }
}
