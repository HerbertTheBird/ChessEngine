import java.util.*;
public class Engine {
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
	 * 12-special flags-pawn moves, castling 11 bits-6 for pawn move 4 for castling 1 for turn
	 */
	long[] boards = {
			65280L, 
			71776119061217280L, 
			66L, 
			4755801206503243776L, 
			36L, 
			2594073385365405696L, 
			129L, 
			-9151314442816847872L, 
			8L, 
			576460752303423488L, 
			16L, 
			1152921504606846976L, 
			0L
	};
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

	int side2move;
	final long[][] zobrist_hash = new long[64][12];
	final long[] zobrist_pawn = new long[8];
	final long[] zobrist_castle = new long[16];
	final long[] zobrist_turn = new long[2];
	final long[] zobrist_depth = new long[50];
	final int CHECKMATE_SCORE = Integer.MAX_VALUE/2;
	int MAX_DEPTH = 5;
	int[] best_move = new int[2];
	/*
	 * TODO
	 * initialize boards arrays
	 * implement check and checkmate logic in eval or movegenerator class
	 * write the engine class - searching for the best move
	 * * do minimax
	 * * do bestmove function
	 * 
	 * 
	 */
	Eval eval;
	MoveGenerator move_gen;
	HashMap<Long, Integer> table = new HashMap();




	public static void print(long[] bitboards) {
		char[][] board = new char[8][8];
		// Initialize the board with empty squares
		for (int row = 0; row < 8; row++) {
			for (int col = 0; col < 8; col++) {
				board[row][col] = '.';
			}
		}

		char[] pieceChars = {'P', 'p', 'N', 'n', 'B', 'b', 'R', 'r', 'Q', 'q', 'K', 'k', 'W', 'B', '#'};
		// Only loop through the first 12 bitboards (individual pieces)
		for (int i = 0; i < 12; i++) {
			long bitboard = bitboards[i];
			for (int position = 0; position < 64; position++) {
				long bit = 1L << position;
				if ((bitboard & bit) != 0) {
					// Calculate row and column from the position
					int row = (position / 8);
					int col = position % 8;
					board[row][col] = pieceChars[i];
				}
			}
		}

		// Print the board
		for (int row = 7; row >=0; row--) {
			for (int col = 0; col < 8; col++) {
				System.out.print(board[row][col] + " ");
			}
			System.out.println();
		}
	}
	public static void main(String args[]) {
		//move 16-24 sneaks in as a pawn move
		//there is no pawn on 16
		Engine engine = new Engine();
		//print(engine.boards);
		//System.out.println((engine.boards[engine.WHITE_PAWN] >>> 16)&1);

	}







	/*flips the color of a piece or side*/
	public int OTHER(int side) {
		return (side)^1;
	}
	public Engine() {
		Scanner in = new Scanner(System.in);
		eval = new Eval(boards);
		move_gen = new MoveGenerator(boards);
		setup();
		while(true) {
			{
				long start = System.currentTimeMillis();
				get_best_move(MAX_DEPTH, WHITE);
				System.out.println("TIME TAKEN: " + (System.currentTimeMillis()-start));
				print(boards);
				int start_square = best_move[0]&0x3F;
				int end_square = (best_move[0] >>> 6) & 0x3F;
				int promotion = best_move[0] >>> 12;
			System.out.println("BEST MOVE " + start_square + " " + end_square);
			System.out.println("EVAL " + best_move[1]);
			}
			{
				System.out.println("input your move");
				String input = in.nextLine();
				int start_square = ((int)input.charAt(0)-'a')+(input.charAt(1)-1)*8;
				int end_square = ((int)input.charAt(3)-'a')+(input.charAt(4)-1)*8;
				int piece = -1;
				int captured_piece = -1;

				for(int j = WHITE_PAWN; j <= BLACK_KING; j++) {
					if(value(j, start_square)) {
						piece = j;
						break;
					}
				}
				for(int j = WHITE_PAWN; j <= BLACK_KING; j++) {
					if(value(j, end_square)) {
						captured_piece = j;
						break;
					}
				}
				if(captured_piece != -1) {
					unset(captured_piece, end_square);
				}
				//System.out.println(piece + " " + depth);//RETURN -1 FOR SOME PIECE SOMEWHERE DEBUG LATER
				unset(piece, start_square);

				set(piece, end_square);

			}

		}
	}
	public void get_best_move(int depth, int turn) {
		table.clear();
		{
			//System.out.println(-Integer.MIN_VALUE == Integer.MIN_VALUE);
			negamax(depth, -Integer.MAX_VALUE, Integer.MAX_VALUE, turn);
			int move = best_move[0];
			int start_square = move&0x3F;
			int end_square = (move >>> 6) & 0x3F;
			int promotion = move >>> 12;
			int piece = -1;
			int captured_piece = -1;

			for(int j = WHITE_PAWN; j <= BLACK_KING; j++) {
				if(value(j, start_square)) {
					piece = j;
					break;
				}
			}
			for(int j = WHITE_PAWN; j <= BLACK_KING; j++) {
				if(value(j, end_square)) {
					captured_piece = j;
					break;
				}
			}
			boolean is_promote = promotion != PAWN;
			if(captured_piece != -1) {
				unset(captured_piece, end_square);
			}
			//System.out.println(piece + " " + depth);//RETURN -1 FOR SOME PIECE SOMEWHERE DEBUG LATER
			unset(piece, start_square);

			if(is_promote) {
				set(promotion*2+side2move, end_square);
			}
			else {
				set(piece, end_square);
			}
		}
		ArrayList<Integer> moves = move_gen.generate_moves(OTHER(turn), 0, 0);
		System.out.println("OPPONENT RESPONSES: ");
		for(int i = 0; i < moves.size(); i++) {
			int start_square = moves.get(i)&0x3F;
			int end_square = (moves.get(i) >>> 6) & 0x3F;
			int promotion = moves.get(i) >>> 12;
			int captured_piece = -1;
			int piece = -1;
			for(int j = WHITE_PAWN; j <= BLACK_KING; j++) {
				if(value(j, start_square)) {
					piece = j;
					break;
				}
			}
			for(int j = WHITE_PAWN; j <= BLACK_KING; j++) {
				if(value(j, end_square)) {
					captured_piece = j;
					break;
				}
			}
			boolean is_promote = promotion != PAWN;

			if(captured_piece != -1) {
				unset(captured_piece, end_square);
			}
			unset(piece, start_square);
			if(is_promote) {
				set(promotion*2+side2move, end_square);
			}
			else {
				set(piece, end_square);
			}

			System.out.println(start_square + " " + end_square + " " + eval.get_eval(side2move));


			if(is_promote) {
				unset(promotion*2+side2move, end_square);
			}
			else {
				unset(piece, end_square);
			}

			set(piece, start_square);
			if(captured_piece != -1) {
				set(captured_piece, end_square);
			}




		}

	}
	public void setup() {
		Random random = new Random();
		//setup the zobrist hash table
		for(int i = 0; i < zobrist_hash.length; i++) {
			for(int j = 0; j < zobrist_hash[0].length; j++) {
				zobrist_hash[i][j] = random.nextLong();
			}
		}
		for(int i = 0; i < zobrist_pawn.length; i++) {
			zobrist_pawn[i] = random.nextLong();
		}
		for(int i = 0; i < zobrist_castle.length; i++) {
			zobrist_castle[i] = random.nextLong();
		}
		for(int i = 0; i < zobrist_turn.length; i++) {
			zobrist_turn[i] = random.nextLong();
		}
		for(int i = 0; i < zobrist_depth.length; i++) {
			zobrist_depth[i] = random.nextLong();
		}

	}
	public int negamax(int depth, int alpha, int beta, int side2move) {
		if(table.containsKey(hash(depth))) {
			return table.get(hash(depth));
		}
		if(eval.is_checkmate(side2move)) {
			return -CHECKMATE_SCORE-depth;
		}
		if(eval.is_stalemate(side2move)) {
			return 0;
		}
		if(depth == 0) {
			int out = eval.get_eval(side2move);
			table.put(hash(depth), out);
			return out;
		}

		int out = Integer.MIN_VALUE;
		ArrayList<Integer> moves = move_gen.generate_moves(side2move, 0, 0);
		for(int i = 0; i < moves.size(); i++) {
			int move = moves.get(i);
			int start_square = move&0x3F;
			int end_square = (move >>> 6) & 0x3F;
			int promotion = move >>> 12;
		int piece = -1;
		int captured_piece = -1;

		for(int j = WHITE_PAWN; j <= BLACK_KING; j++) {
			if(value(j, start_square)) {
				piece = j;
				break;
			}
		}
		for(int j = WHITE_PAWN; j <= BLACK_KING; j++) {
			if(value(j, end_square)) {
				captured_piece = j;
				break;
			}
		}
		boolean is_promote = promotion != PAWN;
		//dont want to deal with special moves yet
		//remove them for now just to test
		//only special move is promotion
		//make move
		//System.out.println(start_square + " " + end_square + " " + promotion);
		if(start_square == 16 && end_square == 8) {
			//System.out.println();
			//Engine.print(boards);
		}
		if(captured_piece != -1) {
			unset(captured_piece, end_square);
		}
		//System.out.println(piece + " " + depth);//RETURN -1 FOR SOME PIECE SOMEWHERE DEBUG LATER
		unset(piece, start_square);

		if(is_promote) {
			set(promotion*2+side2move, end_square);
		}
		else {
			set(piece, end_square);
		}


		//go deeper
		int eval = -negamax(depth-1, -beta, -alpha, OTHER(side2move));
		if(depth == MAX_DEPTH) {
			System.out.println(start_square + " " + end_square + " " + promotion + " " + eval);
		}
		//unmove
		if(is_promote) {
			unset(promotion*2+side2move, end_square);
		}
		else {
			unset(piece, end_square);
		}

		set(piece, start_square);
		if(captured_piece != -1) {
			set(captured_piece, end_square);
		}







		if(eval > out) {

			out = eval;
			if(depth == MAX_DEPTH) {
				best_move[0] = move;
				best_move[1] = eval;
			}
		}
		alpha = Math.max(alpha, eval);
		if(alpha >= beta) {
			break;
		}
		}

		table.put(hash(depth), out);
		return out;
	}

	//use zobrist hashing to get a semi unique long for a position 
	public long hash(int depth) {
		long out = 0L;
		for(int square = 0; square < 64; square++) {
			for(int i = 0; i < 12; i++) {
				if(value(i, square)) {
					out ^= zobrist_hash[square][i];
					break;
				}
			}
		}
		// Handle special flags from board 15
		long specialFlags = boards[12];
		// Extract and handle the turn bit
		if ((specialFlags & 1L) == 1) { // If the least significant bit is 1, it's Black's turn
			out ^= zobrist_turn[1]; // Assuming index 1 is for Black's turn
		} else {
			out ^= zobrist_turn[0]; // Assuming index 0 is for White's turn
		}

		// Extract and handle the castling rights
		long castlingRights = (specialFlags >>> 1) & 0xF; // Shift right by 1 and mask the next 4 bits
		for (int i = 0; i < 4; i++) { // Iterate through each castling right
			if ((castlingRights & (1 << i)) != 0) {
				out ^= zobrist_castle[i];
			}
		}

		// Extract and handle the en passant square
		long enPassantSquare = (specialFlags >>> 5) & 0x3F; // Shift right by 5 and mask the next 6 bits
		if (enPassantSquare != 0) { // Assuming 0 means no en passant available
			// Map enPassantSquare to the file (0-7), assuming enPassantSquare directly represents the square
			int file = (int) enPassantSquare % 8; // Adjust this calculation based on your representation
			out ^= zobrist_pawn[file];
		}

		// Handle search depth, ensure it does not exceed array bounds
		if (depth < zobrist_depth.length) {
			out ^= zobrist_depth[depth];
		}


		return out;
	}

	//finds the bit at position sq on bitboard piece
	public boolean value(int piece, int sq) { // COMPLETE
		return ((boards[piece] >>> sq)&1) == 1;
	}
	public void set(int piece, int sq) {
		boards[piece] |= 1L << sq;
	}
	public void unset(int piece, int sq) {
		boards[piece] &= ~(1L << sq);
	}
}
