/*** Copyright 2003 by Gregory L. Guerin.** Terms of use:**  - Briefly: OPEN SOURCE under Artistic License -- credit fairly, use freely.**  - Fully: <http://www.amug.org/~glguerin/sw/artistic-license.html>*/package app.util;import java.awt.*;import java.awt.event.*;// --- Revision History ---// 25Jul2003 GLG  create/**** BasicAlert is a basic alert Dialog.*/public class BasicAlert  extends Dialog  implements ActionListener, WindowListener{	/** Bit-mask requesting a vertical scoll-bar for the text. */	public static final int SCROLL_VERT = 2;	/** Bit-mask requesting a horizontal scoll-bar for the text. */	public static final int SCROLL_HORZ = 1;	// -- IMPLEMENTATION NOTE --	// The SCROLL_XXX bit-masks are the 1's-complement of the TextArea.SCROLLBAR_XXX	// named constants.  Thus, the value given for 'scrollbars' in the BasicAlert constructor	// can be turned into a TextArea-appropriate value simply by inverting and masking.	private Button ok;	private TextArea box;	public	BasicAlert( Frame frame, String title, boolean isModal,			String text, int rows, int cols, int scrollbars,			float northern, float western )	{		super( frame, title, isModal );		addWindowListener( this );		// Can't resize or zoom, by default.  Caller can change before show()'ing this.		setResizable( false );		setLayout( new BorderLayout( 0, 0 ) );		setFont( new Font( "Dialog", Font.PLAIN, 12 ) );		setBackground( Color.white );		// Turn my scrollbars bit-mask into TextArea named constant values.		scrollbars = 0x03 & ~scrollbars;		Panel into = new Panel();		into.setLayout( new FlowLayout( FlowLayout.CENTER, 16, 16 ) );		box = makeTextArea( text, rows, cols, scrollbars );		box.setEditable( false );		into.add( box );		add( BorderLayout.CENTER, into );		ok = new Button( "OK" );		ok.addActionListener( this );		into = new Panel();		into.setLayout( new FlowLayout( FlowLayout.CENTER, 0, 10 ) );		into.add( ok );		add( BorderLayout.SOUTH, into );		pack();		setLocation( position( this, northern, western ) );	}	/**	** Can override in subclasses to change look.	** The scrollbars value is appropriate for a TextArea constructor.	*/	protected TextArea	makeTextArea( String text, int rows, int cols, int scrollbars )	{		TextArea box = new TextArea( text, rows, cols, scrollbars );		box.setFont( new Font( "SansSerif", Font.PLAIN, 12 ) );		box.setBackground( Color.white );		return ( box );	}	/** Can call externally to manipulate TextArea's attributes or contents. */	public TextArea	getTextArea()	{  return ( box );  }	/**	** Use the Window's current dimensions to determine it's desired top-left position.	** Does not actually change the position of the Window, only returns a Point	** representing what its weighted position would be.	*/	public Point	position( Window target, float northern, float western ) 	{		Rectangle targetRect = target.getBounds();		Dimension targetSize = targetRect.getSize();				Dimension screenSize = target.getToolkit().getScreenSize();		// Returns a replica of Rectangle's location.  We modify and return this replica.		Point result = targetRect.getLocation();		// A value will be negative if the Window's dimension exceeds the screen's dimension.		int above = (int) (northern * (screenSize.height - targetSize.height));		int beside = (int) (western * (screenSize.width - targetSize.width));		// Negative values are ignored, because the position is untenable.		if ( above > 0 )			result.y = above;				if ( beside > 0 )			result.x = beside;				return ( result );	}	/** Go away. */	protected void	goAway()	{		setVisible( false );  		dispose();	}	/** Doesn't matter what the action was, we always goAway(). */	public void 	actionPerformed( ActionEvent what ) 	{  goAway();  }	/** As WindowListener -- a click in the close-box will goAway(). */	public void	windowClosing( WindowEvent what )	{  goAway();  }	/** As WindowListener -- "OK" gets focus. */	public void	windowActivated( WindowEvent what )	{  ok.requestFocus();  }	/** As WindowListener -- do nothing. */	public void	windowClosed( WindowEvent what )	{  return;  }	/** As WindowListener -- do nothing. */	public void	windowDeactivated( WindowEvent what )	{  return;  }	/** As WindowListener -- "OK" gets focus. */	public void	windowDeiconified( WindowEvent what )	{  ok.requestFocus();  }	/** As WindowListener -- do nothing. */	public void	windowIconified( WindowEvent what )	{  return;  }	/** As WindowListener -- "OK" gets focus. */	public void	windowOpened( WindowEvent what )	{  ok.requestFocus();  }}