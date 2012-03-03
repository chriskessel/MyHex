package kessel.hex.admin;

import kessel.hex.domain.Army;
import kessel.hex.domain.GameItem;
import kessel.hex.orders.army.ArmyTransfer;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** A static holder for army transfer actions. */
public class ArmyTransferActions
{
  /** @return the order or null if the order type isn't right for an implied order. */
  public static Action createAction( PlayerDetailsPane playerDetailsPane, Class orderType, GameItem item )
  {
    // There's only one type of army transfer, but keeping this pattern of action creation makes it look like other action types.
    if ( orderType.equals( ArmyTransfer.class ) )
    {
      return new ArmyTransferAction( playerDetailsPane, (Army) item );
    }
    else
    {
      return null;
    }
  }

  static class ArmyTransferAction extends OrderAction
  {
    protected final Army _subject;
    protected Army _target;
    protected final PlayerDetailsPane _playerDetailsPane;

    ArmyTransferAction( PlayerDetailsPane playerDetailsPane, Army subject )
    {
      super( "Transfer" );
      _subject = subject;
      this._playerDetailsPane = playerDetailsPane;
    }

    public void actionPerformed( ActionEvent e )
    {
      // Show all the player's as transfer targets since post-movement the armies may be in the same space.
      List<Army> targets = new ArrayList<>( _playerDetailsPane.getPlayer().getArmies() );
      for ( Iterator<Army> iterator = targets.iterator(); iterator.hasNext(); )
      {
        Army army = iterator.next();
        if ( army.equals( _subject ) )
        {
          iterator.remove();
        }
      }

      if ( !targets.isEmpty() )
      {
        ArmyTransferDialog chooser = new ArmyTransferDialog( _playerDetailsPane._gameDisplay, targets );
        chooser.setVisible( true );

        List<GameItem> transfers = chooser.getTransferItems();
        if ( !transfers.isEmpty() )
        {
          ArmyTransfer order = new ArmyTransfer( _subject, chooser.getTransferTarget(), transfers );
          _subject.addNextTurnOrder( order );
          _playerDetailsPane.updateOrders( order );
          _playerDetailsPane.updateDetails();
        }
      }
      else
      {
        JOptionPane.showMessageDialog( _playerDetailsPane._gameDisplay, "No army is available to accept the transfer." );
      }
    }

    class ArmyTransferDialog extends JDialog implements ActionListener
    {
      JComboBox<Army> _targetList;
      JList<GameItem> _transferItems;

      @SuppressWarnings({ "ThisEscapedInObjectConstruction" })
      private ArmyTransferDialog( Frame frame, List<Army> targets )
      {
        super( frame, "Select Items to Transfer", true );
        JPanel mainPanel = new JPanel( new BorderLayout() );

        // Create the panel letting the user select what to transfer.
        JPanel subjectPanel = new JPanel( new BorderLayout() );
        subjectPanel.setBorder( BorderFactory.createEtchedBorder() );
        JLabel subjectLabel = new JLabel( "Transfer from: " + _subject.getName() );
        _transferItems = new JList( _subject.getUnits().toArray() );
        _transferItems.setSelectionMode( ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );
        _transferItems.setVisibleRowCount( 10 );
        JScrollPane listScroller = new JScrollPane( _transferItems );
        subjectPanel.add( subjectLabel, BorderLayout.NORTH );
        subjectPanel.add( listScroller, BorderLayout.CENTER );

        // Ask for the target army.
        JPanel targetPanel = new JPanel( new BorderLayout() );
        targetPanel.setBorder( BorderFactory.createEtchedBorder() );
        JLabel targetLabel = new JLabel( "Transfer to: " );
        _targetList = new JComboBox( targets.toArray() );
        _targetList.addActionListener( this );
        targetPanel.add( targetLabel, BorderLayout.WEST );
        targetPanel.add( _targetList, BorderLayout.CENTER );

        // Add the OK/Cancel buttons.
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder( BorderFactory.createEtchedBorder() );
        final JButton okButton = new JButton( "OK" );
        okButton.setActionCommand( "OK" );
        okButton.addActionListener( this );
        getRootPane().setDefaultButton( okButton );
        final JButton cancelButton = new JButton( "Cancel" );
        cancelButton.setActionCommand( "Cancel" );
        cancelButton.addActionListener( this );
        buttonPanel.add( okButton, BorderLayout.WEST );
        buttonPanel.add( cancelButton, BorderLayout.EAST );

        mainPanel.add( subjectPanel, BorderLayout.NORTH );
        mainPanel.add( targetPanel, BorderLayout.CENTER );
        mainPanel.add( buttonPanel, BorderLayout.SOUTH );
        add( mainPanel, BorderLayout.CENTER );

        pack();
        setLocationRelativeTo( frame );
      }

      //Handle clicks on the Set and Cancel buttons.
      public void actionPerformed( ActionEvent e )
      {
        switch ( e.getActionCommand() )
        {
          case "OK":
            setVisible( false );
            break;
          case "Cancel":
            _transferItems.clearSelection();
            setVisible( false );
            break;
        }
      }

      public List<GameItem> getTransferItems() { return _transferItems.getSelectedValuesList(); }

      public Army getTransferTarget() { return (Army) _targetList.getSelectedItem(); }
    }
  }
}