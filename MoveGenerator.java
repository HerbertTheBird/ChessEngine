import java.util.*;
public class MoveGenerator {
	/*
	 * 
	 * 0-white pawns
	 * 1-black pawns
	 * 2-white knights
	 * 3-black knights
	 * 4-white bishops
	 * 5-black bishops
	 * 6-white rooks
	 * 7-black rooks
	 * 8-white queens
	 * 9-black queens
	 * 10-white king
	 * 11-black king
	 * 12-special flags-pawn moves, castling
	 */
	/*boards array stores bitboards of all the pieces. 1 for piece here, 0 for not*/
	long[] boards;
	final int PAWN = 0;
	final int KNIGHT = 1;
	final int BISHOP = 2;
	final int ROOK = 3;
	final int QUEEN = 4;
	final int KING = 5;
	final int WHITE = 0;
	final int BLACK = 1;
	final int WHITE_PAWN   = PAWN*2   +WHITE;
	final int BLACK_PAWN   = PAWN*2   +BLACK;
	final int WHITE_KNIGHT = KNIGHT*2 +WHITE;
	final int BLACK_KNIGHT = KNIGHT*2 +BLACK;
	final int WHITE_BISHOP = BISHOP*2 +WHITE;
	final int BLACK_BISHOP = BISHOP*2 +BLACK;
	final int WHITE_ROOK   = ROOK*2   +WHITE;
	final int BLACK_ROOK   = ROOK*2   +BLACK;
	final int WHITE_QUEEN  = QUEEN*2  +WHITE;
	final int BLACK_QUEEN  = QUEEN*2  +BLACK;
	final int WHITE_KING   = KING*2   +WHITE;
	final int BLACK_KING   = KING*2   +BLACK;
	/*gets color of piece*/
	public int PCOLOR(int p) { // COMPLETE
		return (p)&1;
	}
	/*flips the color of a piece or side*/
	public int OTHER(int side) { // COMPLETE
		return (side)^1;
	}
	/*these 2 arrays pre-compute moves for the knight and king so they can be used faster later*/
	long[] knight_moves = new long[64];
	long[] king_moves = new long[64];
	public MoveGenerator(long[] boards) { // COMPLETE
		this.boards = boards;
		setup();
	}
	public void setup() { // COMPLETE
		/*pre-compute the knight and king moves*/
		for(int i = 0; i < 64; i++) {
			long posBitboard = 0L;
			int[] dx = {-2, -2, -1, -1, 1, 1, 2, 2};
			int[] dy = {-1, 1, 2, -2, 2, -2, 1, -1};
			int x = i%8;
			int y = i/8;
			for(int j = 0; j < 8; j++) {
				if(x+dx[j] < 8 && x+dx[j] >= 0 && y+dy[j] < 8 && y+dy[j] >= 0) {
					long bitmask = 1L << (x+dx[j])+(y+dy[j])*8;
					posBitboard |= bitmask;
				}
			}
			knight_moves[i] = posBitboard;
			
		}
		for(int i = 0; i < 64; i++) {
			long posBitboard = 0L;
			int x = i%8;
			int y = i/8;
			for(int dx = -1; dx <= 1 && dx+x < 8 && dx+x >= 0; dx++) {
				for(int dy = -1; dy <= 1 && dy+y < 8 && dy+y >= 0; dy++) {
					if(x == 0 && y == 0)
						continue;
					posBitboard |= 1L << (x+dx)+(y+dy)*8;
				}
			}
			king_moves[i] = posBitboard;
		}
	}
	/*
	 * moves will be stored in int form for speed
	 * first 6 bits are starting square
	 * second 6 bits are ending square
	 * 2 bits for castling kingside and queenside
	 * 1 bit for en passant
	 * 4 bits for promotion
	 * if promotion is set to PAWN, that means there is no promotion
	 */
	public ArrayList<Integer> generate_moves(int side2move, int pawn_move, int can_castle) { // INCOMPLETE NEED TO CHECK FOR LEGAL MOVES
		ArrayList<Integer> moves = new ArrayList();
		long side2moveboard = 0L;
		long all_pieces = 0L;
		for(int i = side2move; i <= BLACK_KING; i+=2) {
			side2moveboard |= boards[i];
		}
		for(int i = side2move; i <= BLACK_KING; i++) {
			all_pieces |= boards[i];
		}
		for(int i = 0; i < 64; i++) {
			//check if there is a piece here that is the same color as the player to move
			if(value_l(side2moveboard, i)) {
				
				int piece = -1;
				int color = -1;
				//search bitboards to determine what the piece is
				for(int j = side2move; j <= BLACK_KING; j+=2) {
					if(value(j, i)) {
						piece = j;
						break;
					}
				}
				int x = i%8;
				int y = i/8;
				//type of piece
				piece = piece/2;

				if(piece == PAWN) {
					//calculates pawn moves and captures seperately
					//can deal with promotion
					int direction = (side2move==WHITE)?8:-8;
					pawn_move(moves, i, i+direction, all_pieces);
					pawn_double_move(moves, i, i+direction*2, all_pieces);
					
					//capture includes en passant
					//not yet
					pawn_capture(moves, i, i+direction+1, side2move, pawn_move);
					pawn_capture(moves, i, i+direction-1, side2move, pawn_move);
				}
				else if(piece == KNIGHT) {
					//knight and king use precomputation to generate moves
					knight_move(moves, i, side2move);
				}
				else if(piece == BISHOP) {
					//bishop and rook both sliding pieces, use sliding_move function for simplicity
					sliding_move(moves, i, 1, 1, side2moveboard, all_pieces);
					sliding_move(moves, i, 1, -1, side2moveboard, all_pieces);
					sliding_move(moves, i, -1, 1, side2moveboard, all_pieces);
					sliding_move(moves, i, -1, -1, side2moveboard, all_pieces);
				}
				else if(piece == ROOK) {
					sliding_move(moves, i, 1, 0, side2moveboard, all_pieces);
					sliding_move(moves, i, 0, 1, side2moveboard, all_pieces);
					sliding_move(moves, i, -1, 0, side2moveboard, all_pieces);
					sliding_move(moves, i, 0, -1, side2moveboard, all_pieces);
				}
				else if(piece == QUEEN) {
					//bishop
					sliding_move(moves, i, 1, 1, side2moveboard, all_pieces);
					sliding_move(moves, i, 1, -1, side2moveboard, all_pieces);
					sliding_move(moves, i, -1, 1, side2moveboard, all_pieces);
					sliding_move(moves, i, -1, -1, side2moveboard, all_pieces);
					//rook
					sliding_move(moves, i, 1, 0, side2moveboard, all_pieces);
					sliding_move(moves, i, 0, 1, side2moveboard, all_pieces);
					sliding_move(moves, i, -1, 0, side2moveboard, all_pieces);
					sliding_move(moves, i, 0, -1, side2moveboard, all_pieces);
				}
				else if(piece == KING) {
					//knight and king use precomputation to generate moves
					king_move(moves, i, side2move);
				}

			}
		}
		//generates pseudo legal moves - moves can leave king in check or move into check
		//filter for legal moves here

		return moves;
	}
	public void king_move(ArrayList<Integer> moves, int start_square, int side2move) { // COMPLETE
		//same as knight but with a king
		//GENERATES PSEUDO LEGAL MOVES
		long king = king_moves[start_square]&(~boards[side2move]);
		for(int i = 0; i < 64; i++) {
			if(((king >> i)&1) == 1) {
				moves.add(move_value(start_square, i, PAWN));
			}
		}
	}
	public void sliding_move(ArrayList<Integer> moves, int start_square, int dx, int dy, long side2moveboard, long all_pieces) { // COMPLETE
		int x = start_square%8;
		int y = start_square/8;
		for(int x_i = x+dx, y_i = y+dy; x_i < 8 && x_i >= 0 && y_i < 8 && y_i >= 0; x_i+=dx, y_i+=dy) {
			int i = y_i*8+x_i;
			if(!value_l(side2moveboard, i)) {
				moves.add(move_value(start_square, i, PAWN));
			}
			if(value_l(all_pieces, i)) {
				return;
			}
		}
	}
	public void knight_move(ArrayList<Integer> moves, int start_square, int side2move) { // COMPLETE
		//get the precomputed knight moves and & it with the bitboard with the pieces of the same color so it doesnt capture its own pieces
		long knight = knight_moves[start_square]&(~boards[side2move]);
		//check for places it can move
		for(int i = 0; i < 64; i++) {
			if(((knight >> i)&1) == 1) {
				moves.add(move_value(start_square, i, PAWN));
			}
		}
	}
	
	//handle pawn moves and captures seperately
	public void pawn_move(ArrayList<Integer> moves, int start_square, int end_square, long all_pieces) { // COMPLETE
		int x = start_square%8;
		int y = start_square/8;
		int end_x = end_square%8;
		int end_y = end_square/8;
		//check if it can move to the square above - square is valid and empty
		if(can_move(end_square, all_pieces)) {
			//check for promotion
			if(end_y == 7 || end_y == 0) {
				for(int promotion = KNIGHT; promotion <= QUEEN; promotion++) {
					moves.add(move_value(start_square, end_square, promotion));
				}
			}
			else {
				moves.add(move_value(start_square, end_square, PAWN));
			}
		}
	}
	public void pawn_double_move(ArrayList<Integer> moves, int start_square, int end_square, long all_pieces) { // COMPLETE
		int x = start_square%8;
		int y = start_square/8;
		int end_x = end_square%8;
		int end_y = end_square/8;
		if(!((y == 1 && end_y == 3)||(y == 6 && end_y == 4))) {
			return;
		}
		//check if it can move to the square above - square is valid and empty
		if(can_move(end_square, all_pieces) && can_move((end_square+start_square)/2, all_pieces)) {
			moves.add(move_value(start_square, end_square, PAWN));
		}
	}
	//capture for pawns
	public void pawn_capture(ArrayList<Integer> moves, int start_square, int end_square, int side2move, int pawn_move) { // COMPLETE
		int x = start_square%8;
		int y = start_square/8;
		int end_x = end_square%8;
		int end_y = end_square/8;
		//check if capture or en passant as they look the same for a pawn                 make sure the you dont accidentily capture across the board
		//if((can_capture(end_square, side2move)||can_en_passant(end_square, side2move, pawn_move)) && Math.abs(x-end_x) == 1){
		if(can_capture(end_square, side2move) && Math.abs(x-end_x) == 1){
			//promotion check
			if(end_y == 7 || end_y == 0) {
				for(int promotion = KNIGHT; promotion <= QUEEN; promotion++) {
					moves.add(move_value(start_square, end_square, promotion));
				}
			}
			else {
				moves.add(move_value(start_square, end_square, PAWN));
			}
		}
	}
	
	//check is move(not capture) is possible
	public boolean can_move(int sq, long all_pieces) { // COMPLETE
		return is_valid(sq) && !value_l(all_pieces, sq);
	}
	//check if capture is possible
	public boolean can_capture(int sq, long side2moveboard) { // COMPLETE
		return is_valid(sq) && value_l(side2moveboard, sq);
	}
	//function for checking if en passant is possible
	public boolean can_en_passant(int sq, int side2move, int pawn_move) { // COMPLETE
		return is_valid(sq) && ((side2move == WHITE && sq == pawn_move+8) || (side2move == BLACK && sq == pawn_move-8));
	}
	//checks if the square is on the chess board
	public boolean is_valid(int sq) { // COMPLETE
		return sq < 64 && sq >= 0;
	}
	//finds the bit at position sq on bitboard piece
	public boolean value(int piece, int sq) { // COMPLETE
		return ((boards[piece] >> sq)&1) == 1;
	}
	public boolean value_l(long board, int sq) { // COMPLETE
		return ((board >> sq)&1) == 1;
	}
	//this gives the function to store the move information in a single int
	public int move_value(int start_square, int end_square, int promotion) { // COMPLETE
		//System.out.println(start_square + " " + end_square + " " + promotion);
		return (start_square)|(end_square << 6)|(promotion << 12);
	}
}
