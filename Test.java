
public class Test {
	public static void main(String[] args) {
		char[][] board = {
				{'r', 'n', 'b', 'q', 'k', 'b', 'n', 'r'}, // 8
				{'p', 'p', 'p', 'p', 'p', 'p', 'p', 'p'}, // 7
				{'.', '.', '.', '.', '.', '.', '.', '.'}, // 6
				{'.', '.', '.', '.', '.', '.', '.', '.'}, // 5
				{'.', '.', '.', '.', '.', '.', '.', '.'}, // 4
				{'.', '.', '.', '.', '.', '.', '.', '.'}, // 3
				{'P', 'P', 'P', 'P', 'P', 'P', 'P', 'P'}, // 2
				{'R', 'N', 'B', 'Q', 'K', 'B', 'N', 'R'}  // 1
				// a    b    c    d    e    f    g    h
		}; 
		long[] bitboards = new long[15]; // 12 for individual pieces, 3 for groups

		// Map from character to bitboard index
		int[] pieceToIndex = new int[128]; // ASCII size
		pieceToIndex['p'] = 0;
		pieceToIndex['P'] = 1;
		pieceToIndex['n'] = 2;
		pieceToIndex['N'] = 3;
		pieceToIndex['b'] = 4;
		pieceToIndex['B'] = 5;
		pieceToIndex['r'] = 6;
		pieceToIndex['R'] = 7;
		pieceToIndex['q'] = 8;
		pieceToIndex['Q'] = 9;
		pieceToIndex['k'] = 10;
		pieceToIndex['K'] = 11;

		for (int row = 0; row < board.length; row++) {
			for (int col = 0; col < board[row].length; col++) {
				char piece = board[row][col];
				if (piece != '.') {
					int index = pieceToIndex[piece];
					long bit = 1L << (row * 8 + col);
					bitboards[index] |= bit; // Set bit for individual piece type

					if (Character.isLowerCase(piece)) {
						bitboards[12] |= bit; // White pieces
					} else {
						bitboards[13] |= bit; // Black pieces
					}
					bitboards[14] |= bit; // All pieces
				}
			}
		}
		for(int i = 0; i < 15; i++) {
			System.out.println(bitboards[i]+"L, ");
		}
	}
}
