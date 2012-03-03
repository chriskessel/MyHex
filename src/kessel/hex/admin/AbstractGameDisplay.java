package kessel.hex.admin;

import kessel.hex.domain.GameEvent;
import kessel.hex.domain.GameItem;
import kessel.hex.map.ControlLevel;
import kessel.hex.map.Location;
import kessel.hex.map.Region;
import kessel.hex.util.HexCalculator;
import kessel.hex.util.Tuple;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Map;

/** Common functionality for both Admin and Player displays. */
@SuppressWarnings({ "ObjectEquality", "AbstractClassExtendsConcreteClass" })
public abstract class AbstractGameDisplay extends JFrame implements MouseListener
{
  static final int HEX_SIDES = 6;
  static final int CELL_RADIUS = 30; // Results in a 60x50 hex.
  static final HexCalculator CELL_METRICS = new HexCalculator( CELL_RADIUS );

  protected AbstractMapPane _mapPane;
  protected StatusPane _statusPane;
  protected AbstractDetailsPane _detailsPane;
  protected EventsPane _eventsPane;

  protected Tuple _selectedHex = null;
  protected OrderAction _actionInProgress;

  @SuppressWarnings({ "AbstractMethodCallInConstructor", "ThisEscapedInObjectConstruction" })
  protected AbstractGameDisplay()
  {
    doAddWindowListener( this );

    addMenuBar();

    // A pane for the entire center area: map, status.
    JPanel contentPane = new JPanel( new BorderLayout() );
    doAddKeyListener( contentPane );
    setContentPane( contentPane );
    contentPane.setBorder( BorderFactory.createEtchedBorder() );

    // ..add the map.
    JPanel westContainer = new JPanel( new BorderLayout() );
    _mapPane = createMapPane();
    _mapPane.addMouseListener( this );
    JScrollPane mapContainer = new JScrollPane( _mapPane );
    mapContainer.setBorder( BorderFactory.createEtchedBorder() );

    // ..add the status area.
    JPanel statusPaneContainer = new JPanel( new BorderLayout() ); // hack to deal with text area sizing bug.
    statusPaneContainer.setAlignmentX( Component.LEFT_ALIGNMENT );
    _statusPane = createStatusPane();
    _statusPane.setEditable( false );
    statusPaneContainer.add( _statusPane, BorderLayout.WEST );
    westContainer.add( mapContainer, BorderLayout.CENTER );
    westContainer.add( statusPaneContainer, BorderLayout.SOUTH );

    // The side pane with all the currently relevant details (e.g. selected hex info).
    _detailsPane = createDetailsPane();
    _detailsPane.setBorder( BorderFactory.createEtchedBorder() );

    // A bottom pane with a list of the game events from the previously run turn.
    _eventsPane = createEventsPane();
    _eventsPane.setBorder( BorderFactory.createEtchedBorder() );

    contentPane.add( westContainer, BorderLayout.CENTER );
    contentPane.add( _detailsPane, BorderLayout.EAST );
    contentPane.add( _eventsPane, BorderLayout.SOUTH );
  }

  protected abstract void addMenuBar();

  private void doAddWindowListener( AbstractGameDisplay gameDisplay )
  {
    gameDisplay.addWindowListener( new WindowAdapter()
    {
      public void windowClosing( WindowEvent e )
      {
        e.getWindow().setVisible( false );
        e.getWindow().dispose();
        System.exit( 0 );
      }
    } );
  }

  /** Support cancelling an order's target acquisition process by hitting the escape key. */
  private void doAddKeyListener( JPanel gameDisplay )
  {
    Action action = new AbstractAction( "Escape Handler" )
    {
      public void actionPerformed( ActionEvent e )
      {
        if ( (_actionInProgress != null) && (_actionInProgress instanceof TargetAction) )
        {
          ((TargetAction) _actionInProgress).cancelTargetAcquisition();
        }
      }
    };

    KeyStroke keyStroke = KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE, 0 );
    gameDisplay.getActionMap().put( action.getValue( Action.NAME ), action );
    gameDisplay.getInputMap( JComponent.WHEN_IN_FOCUSED_WINDOW ).put( keyStroke, action.getValue( Action.NAME ) );
  }

  protected abstract AbstractMapPane createMapPane();

  protected abstract AbstractDetailsPane createDetailsPane();

  protected StatusPane createStatusPane() { return new StatusPane( this ); }

  protected EventsPane createEventsPane() { return new EventsPane( this );}

  public void init()
  {
    _selectedHex = null;
    _actionInProgress = null;
    setVisible( false );
    _mapPane.init();
    _statusPane.init();
    _detailsPane.init();
    _eventsPane.init();
    revalidate();
    Dimension preferredSize = getPreferredSize();
    setSize( preferredSize );
    pack();
    repaint();
    setVisible( true );
  }

  public Tuple getSelectedHex() { return _selectedHex; }

  public void setSelectedHex( Tuple selectedHex )
  {
    if ( _selectedHex != null )
    {
      repaintHex( _selectedHex );
    }
    _selectedHex = selectedHex;
    _detailsPane.updateDetails();
    validate();
    repaint();
  }

  void repaintHex( Tuple hex )
  {
    HexCalculator cellMetrics = getCellMetrics();
    Tuple pixelCenter = cellMetrics.getPixelCenterByGrid( hex );
    _mapPane.repaint(
      pixelCenter.x - cellMetrics._radius * 10, pixelCenter.y - (cellMetrics._height * 10 / 2),
      cellMetrics._width * 10, cellMetrics._height * 10 );
  }

  public void setActionInProgress( OrderAction actionInProgress ) { _actionInProgress = actionInProgress; }

  public Location getLocationAtHex( Tuple hex ) { return _mapPane.getLocations().get( hex ); }

  protected List<GameItem> getItemsAtHex( Tuple hex ) { return _mapPane.getGameItems( hex ); }

  public HexCalculator getCellMetrics() { return CELL_METRICS; }

  public Map<String, Color> getPlayerColors() { return _mapPane.getPlayerColors(); }

  // ----------- A variety of game-aware utility method whose implementations depend on the subclass ------------
  protected abstract boolean isOnMap( Tuple coord );

  public abstract List<GameEvent> getGameEvents();

  public abstract boolean isLoaded();

  public abstract List<Region> getRegions();

  public abstract Integer getEmbassyLevel( Region region, String playerName );

  public abstract ControlLevel getControlLevel( Region region, String playerName );

  public abstract List<String> getPlayerNames();

  protected abstract int getMapHeight();

  protected abstract int getMapWidth();

  // ----------- handle mouse events -----------------
  public void mouseClicked( MouseEvent e )
  {
    if ( isLoaded() )
    {
      if ( _actionInProgress == null )
      {
        Tuple newHex = getCellMetrics().getGridByPixel( e.getX(), e.getY() );
        if ( isOnMap( newHex ) )
        {
          setSelectedHex( newHex );
        }
      }
      else
      {
        // Are we picking a target? If so, give the mouse click to the target action.
        if ( _actionInProgress instanceof TargetAction )
        {
          ((TargetAction) _actionInProgress).getTargetChooser().mouseClicked( e );
        }
      }
    }
  }

  public void mousePressed( MouseEvent e )
  {
    if ( _actionInProgress instanceof TargetAction )
    {
      ((TargetAction) _actionInProgress).getTargetChooser().mousePressed( e );
    }
  }

  public void mouseReleased( MouseEvent e ) {}

  public void mouseEntered( MouseEvent e ) {}

  public void mouseExited( MouseEvent e ) {}
}