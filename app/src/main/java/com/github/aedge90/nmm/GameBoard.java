package com.github.aedge90.nmm;

import java.util.LinkedList;

import android.support.annotation.VisibleForTesting;
import android.util.Log;

public abstract class GameBoard {

    final static int LENGTH = 7;
    final static GameBoardPosition N = new GameBoardPosition(0,0);
    final static GameBoardPosition I = null;

    GameBoardPosition[][] field;

    private LinkedList<Position> allValidPositions = new LinkedList<Position>();

    public static final int REMISMAX = 40;

    private int remisCount = 0;
    private int remisCountBeforeKill = 0;

    enum GameState {
        RUNNING, REMIS, WON_NO_MOVES, WON_KILLED_ALL
    }

    GameBoard(){}

    @VisibleForTesting
    GameBoard(Options.Color[][] inputField) {
        initField();
        if (inputField.length != LENGTH || inputField[0].length != LENGTH){
            throw new IllegalArgumentException("Constructor called with wrong size of array");
        }
        for(int y = 0; y < LENGTH; y++){
            for(int x = 0; x < LENGTH; x++){
                if(getGameBoardPosAt(x,y) == I && !inputField[y][x].equals(Options.Color.INVALID)) {
                    throw new IllegalArgumentException("Constructor called with invalid input field");
                }
                if(getGameBoardPosAt(x,y) != I){
                    if(inputField[y][x].equals(Options.Color.BLACK)) {
                        getGameBoardPosAt(x,y).setColor(Options.Color.BLACK);
                    }else if (inputField[y][x].equals(Options.Color.WHITE)){
                        getGameBoardPosAt(x,y).setColor(Options.Color.WHITE);
                    }else if (inputField[y][x].equals(Options.Color.NOTHING)){
                        getGameBoardPosAt(x,y).setColor(Options.Color.NOTHING);
                    }
                }
            }
        }
    }

    void initGameBoardPositionsFrom(GameBoard other){
        //copy the field from other
        for(int i = 0; i < LENGTH; i++){
            for(int j = 0; j < LENGTH; j++){
                if(other.field[i][j] != null) {
                    field[i][j].setColor(other.field[i][j].getColor());
                }
            }
        }
    }

    //TODO allValidPositions to GameBoardPosition

    void initGameBoardPositions(){
        for(int i = 0; i < LENGTH; i++){
            for(int j = 0; j < LENGTH; j++){
                if(!(field[i][j] == I)) {
                    field[i][j] = new GameBoardPosition(j,i);
                    field[i][j].setColor(Options.Color.NOTHING);
                    allValidPositions.add(new Position(field[i][j]));
                }
            }
        }
    }

    public abstract void initField();

    @VisibleForTesting
    abstract GameBoard getCopy();

    public LinkedList<Position> getPositions(Options.Color player) {
        LinkedList<Position> result = new LinkedList<Position>();
        for(Position p : allValidPositions){
            if(getGameBoardPosAt(p).getColor().equals(player)) {
                result.add(p);
            }
        }
        return result;
    }

    GameBoardPosition getGameBoardPosAt(int x, int y){
        return field[y][x];
    }

    GameBoardPosition getGameBoardPosAt(Position pos) {
        if (pos == null){
            Log.e("GameBoard", "Error: getGameBoardPositionAt: Position was null!");
        }
        return getGameBoardPosAt(pos.getX(), pos.getY());
    }

    Options.Color getColorAt(int x, int y) {
        if(field[y][x] == I){
            return Options.Color.INVALID;
        }
        return field[y][x].getColor();
    }

    Options.Color getColorAt(Position pos) {
        if (pos == null){
            Log.e("GameBoard", "Error: getColorAt: Position was null!");
        }
        return getColorAt(pos.getX(), pos.getY());
    }
    
    private void makeSetMove(Position dest, Options.Color color) {
        field[dest.getY()][dest.getX()].setColor(color);
    }

    private void makeKillMove(Position dest) {
        field[dest.getY()][dest.getX()].setColor(Options.Color.NOTHING);
    }
    
    private void makeMove(Position src, Position dest, Options.Color color) {
        field[src.getY()][src.getX()].setColor(Options.Color.NOTHING);
        field[dest.getY()][dest.getX()].setColor(color);
    }

    //this executes only the setting or moving phase of a player, regardless if a kill is contained in move
    //necessary to make is separate as the user can only add the kill after this move was done
    void executeSetOrMovePhase(Move move, Player player) {
        remisCount++;
        if(player.getSetCount() > 0){
            if(!getColorAt(move.getDest()).equals(Options.Color.NOTHING)){
                throw new IllegalArgumentException("Player " + player.getColor() + " is trying to set to an occupied field by: " + getColorAt(move.getDest()));
            }
            makeSetMove(move.getDest(), player.getColor());
            player.setSetCount(player.getSetCount() - 1);
        }else{
            if(!getColorAt(move.getDest()).equals(Options.Color.NOTHING)){
                throw new IllegalArgumentException("Player " + player.getColor() + " is trying to move to an occupied field by: " + getColorAt(move.getDest()));
            }
            if(getColorAt(move.getSrc()).equals(Options.Color.NOTHING)){
                throw new IllegalArgumentException("Player " + player.getColor() + " is trying to move from an empty field, which is: " + getColorAt(move.getSrc()));
            }
            makeMove(move.getSrc(), move.getDest(), player.getColor());
        }
    }

    //this executes a kill if the move contains one
    void executeKillPhase(Move move, Player player){
        if(move.getKill() != null){
            remisCountBeforeKill = remisCount;
            remisCount = 0;
            if(getColorAt(move.getKill()).equals(player.getColor())){
                throw new IllegalArgumentException("Trying to kill own piece of color: " + player.getColor());
            }
            if(getColorAt(move.getKill()).equals(Options.Color.NOTHING)){
                throw new IllegalArgumentException("Player " + player.getColor() + " is trying to kill an empty field");
            }
            makeKillMove(move.getKill());
        }
    }

    //this executes the complete turn of a player, including setting or moving and killing
    void executeCompleteTurn(Move move, Player player){
        executeSetOrMovePhase(move, player);
        executeKillPhase(move, player);
    }

    //undoes a complete turn of a player, including setting or moving and killing
    public void reverseCompleteTurn(Move move, Player player) {
        remisCount--;
        if(move.getSrc() == null && move.getDest() != null){
            makeKillMove(move.getDest());
            player.setSetCount(player.getSetCount() + 1);
        }else{
            makeMove(move.getDest(), move.getSrc(), player.getColor());
        }
        if(move.getKill() != null){
            remisCount = remisCountBeforeKill;
            makeSetMove(move.getKill(), player.getOtherPlayer().getColor());
        }    
    }

    // returns true if player has a piece in an enemy mill, which the enemy could close if it werent there
    boolean preventedMill (Position p, Player player){
        Position[] preventedMill = getMill(p, player.getOtherPlayer().getColor());
        if(preventedMill == null){
            return false;
        }
        //check if enemy could actually move a piece into the mill in the next move --> prevented mill
        if(player.getOtherPlayer().getSetCount() > 0 || getPositions(player.getOtherPlayer().getColor()).size() == 3){
            return true;
        }
        GameBoardPosition[] neighbors = getGameBoardPosAt(p).getNeighbors();
        for(GameBoardPosition neighbor : neighbors) {
            //check if enemy could actually move a piece that is not part of the mill to form his mill
            if(!preventedMill[0].equals(neighbor) && !preventedMill[1].equals(neighbor) && !preventedMill[2].equals(neighbor)) {
                if (neighbor != null && getColorAt(neighbor).equals(player.getOtherPlayer().getColor())) {
                    return true;
                }
            }
        }
        return false;
    }

    // returns true if two pieces of color player are found, that form a mill together with p
    // the color of p itself is NOT checked
    boolean isInMill(Position p, Options.Color player) {
        if(null != getMill(p, player)){
            //mill was found
            return true;
        }else{
            return false;
        }
    }

    // returns 1 or 2 if one piece of color player is found and one that belongs to nobody
    // and the two position could form a mill together with p later
    // the color of p itself is NOT checked
    // 1 is returned if p is in one potential mill, 2 if it is in two.
    public int isInNPotentialMills(Position p, Options.Color player) {
        Position[] potentialMill = getPotentialMill(p, player);
        if(null == potentialMill){
            return 0;
        }
        for(Position pos : potentialMill){
            Options.Color colorBefore = getGameBoardPosAt(pos).getColor();
            if(!pos.equals(p) && !colorBefore.equals(Options.Color.NOTHING)){
                //remove the player piece of the found potential mill. If there is still one, then it has to be two
                makeSetMove(pos, Options.Color.NOTHING);
                potentialMill = getPotentialMill(p, player);
                makeSetMove(pos, colorBefore);
                if(null != potentialMill){
                    return 2;
                }
            }
        }
        return 1;
    }

    Position[] getPotentialMill (Position p, Options.Color player) {
        return getMillOrPartialMill(p, player, true);
    }

    Position[] getMill(Position p, Options.Color player) {
        return getMillOrPartialMill(p, player, false);
    }

    //if two pieces of color player are found, that form a mill together with position
    //an array containing the two pieces and position is returned, else null is returned
    Position[] getMillOrPartialMill(Position p, Options.Color player, boolean partial) {
        Position[] mill;
        GameBoardPosition checkPos = getGameBoardPosAt(p);
        mill = getMillWithPosInMiddle(checkPos, player, partial);
        if(mill != null){
            return mill;
        }
        mill = getMillWithPosAtCorner(checkPos, player, partial);
        if(mill != null){
            return mill;
        }
        return null;
    }

    protected Position[] getMillWithPosInMiddle(GameBoardPosition middlePos, Options.Color player, boolean partial){
        GameBoardPosition[] neighbors = middlePos.getNeighbors();
        for(GameBoardPosition neighbor : neighbors) {
            if (neighbor != null && middlePos.getOpposite(neighbor) != null) {
                if (belongTo(neighbor, middlePos.getOpposite(neighbor), player, partial)) {
                    return new Position[]{new Position(neighbor), new Position(middlePos), new Position(middlePos.getOpposite(neighbor))};
                }
            }
        }
        return null;
    }

    protected Position[] getMillWithPosAtCorner(GameBoardPosition cornerPos, Options.Color player, boolean partial){
        GameBoardPosition[] neighbors = cornerPos.getNeighbors();
        for(GameBoardPosition neighbor : neighbors) {
            if (neighbor != null && neighbor.getOpposite(cornerPos) != null) {
                if (belongTo(neighbor.getOpposite(cornerPos), neighbor, player, partial)) {
                    return new Position[]{new Position(neighbor.getOpposite(cornerPos)), new Position(neighbor), new Position(cornerPos)};
                }
            }
        }
        return null;
    }


    private boolean belongTo(GameBoardPosition p1, GameBoardPosition p2, Options.Color player, boolean partial){
        if(partial){
            return oneBelongsToPlayerOtherToNobody(p1, p2, player);
        }else{
            return bothBelongTo(p1, p2, player);
        }
    }

    private boolean bothBelongTo(GameBoardPosition p1, GameBoardPosition p2, Options.Color player){
        if(p1.getColor().equals(player) && p2.getColor().equals(player)){
            return true;
        }else{
            return false;
        }
    }

    private boolean oneBelongsToPlayerOtherToNobody(GameBoardPosition p1, GameBoardPosition p2, Options.Color player){
        if(p1.getColor().equals(player) && p2.getColor().equals(Options.Color.NOTHING)){
            return true;
        }else if(p2.getColor().equals(player) && p1.getColor().equals(Options.Color.NOTHING)){
            return true;
        }else{
            return false;
        }
    }

    //is any move possible?
    private boolean movesPossible(Options.Color player, int setCount) {
        LinkedList<Position> positionsOfPlayer = getPositions(player);
        if(setCount > 0){
            return true;
        }
        boolean jump = false;
        if (positionsOfPlayer.size() == 3){
            jump = true;
        }
        if (!jump){
            for (Position p : positionsOfPlayer) {
                if (moveUp(p) != null)
                    return true;
                if (moveDown(p) != null)
                    return true;
                if (moveRight(p) != null)
                    return true;
                if (moveLeft(p) != null)
                    return true;
            }
        } else {
            return true;
        }
        return false;
    }

    private void addnonJumpMoves(LinkedList<Move> moves, Player player){
        for (Position p : getPositions(player.getColor())) {
            if (moveUp(p) != null) {
                addpossibleKillstoMove(moves, moveUp(p), player);
            }
            if (moveDown(p) != null) {
                addpossibleKillstoMove(moves, moveDown(p), player);
            }
            if (moveRight(p) != null) {
                addpossibleKillstoMove(moves, moveRight(p), player);
            }
            if (moveLeft(p) != null) {
                addpossibleKillstoMove(moves, moveLeft(p), player);
            }
        }
    }

    private void addJumpMoves(LinkedList<Move> moves, Player player){
        for (Position all : getAllValidPositions()) {
            if (getColorAt(all).equals(Options.Color.NOTHING)) {
                for (Position own: getPositions(player.getColor())) {
                    addpossibleKillstoMove(moves, new Move(all, own, null), player);
                }
            }
        }
    }

    private void addSetMoves(LinkedList<Move> moves, Player player){
        for (Position all : getAllValidPositions()) {
            if (getColorAt(all).equals(Options.Color.NOTHING)) {
                addpossibleKillstoMove(moves, new Move(all, null, null), player);
            }
        }
    }

    @VisibleForTesting
    void addpossibleKillstoMove(LinkedList<Move> possibleMovessoFar, Move move, Player player){
        boolean inMill = false;
        //this is important so there are no mills wrongly detected when move contains a source and
        //destination that is inside the same mill
        executeSetOrMovePhase(move, player);
        inMill = isInMill(move.getDest(), player.getColor());
        reverseCompleteTurn(move, player);
        //player has a mill after doing this move --> he can kill a piece of the opponent
        if(inMill){
            int added = 0;
            for (Position kill : getPositions(player.getOtherPlayer().getColor())) {
                if(!isInMill(kill, player.getOtherPlayer().getColor())){
                    Move killMove = new Move(move.getDest(), move.getSrc(), kill);
                    // using add first is important, so the kill moves will be at the beginning of the list
                    // by that its more likely that the alpha beta algorithms does more cutoffs
                    possibleMovessoFar.addFirst(killMove);
                    added++;
                }
            }
            //no pieces to kill because all are in a mill --> do it again but now add all pieces
            //as you are allowed to kill if all pieces are part of a mill
            if(added == 0){
                for (Position kill2 : getPositions(player.getOtherPlayer().getColor())) {
                    Move killMove = new Move(move.getDest(), move.getSrc(), kill2);
                    possibleMovessoFar.addFirst(killMove);
                }
            }
        }else{
            possibleMovessoFar.add(move);
        }
    }

    int nEmptyNeighbors(final Position pos){
        int nEmptyNeighbors = 0;
        for(GameBoardPosition neighbor : getGameBoardPosAt(pos).getNeighbors()){
            if(neighbor != null && neighbor.getColor().equals(Options.Color.NOTHING)){
                nEmptyNeighbors++;
            }
        }
        return nEmptyNeighbors;
    }

    //returns a list of moves that the player is able to do
    LinkedList<Move> possibleMoves(Player player) {
        LinkedList<Move> poss = new LinkedList<Move>();
        int nPositions = getPositions(player.getColor()).size();
        //do not compute possible moves if the player has lost, otherwise it breaks the evaluation
        //as a state AFTER loosing would be evaluated instead of the final state after the final kill
        if(nPositions < 3 && player.getSetCount() <= 0){
            return poss;
        }
        if(player.getSetCount() > 0){
            addSetMoves(poss, player);
        }else{
            boolean jump = false;
            if (nPositions <= 3){
                jump = true;
            }
            if (!jump) {
                addnonJumpMoves(poss, player);
            } else {
                addJumpMoves(poss, player);
            }
        }
        return poss;
    }
    
    //is move dest possible?
    boolean movePossible(Position src, Position dest){
        if(!getColorAt(dest).equals(Options.Color.NOTHING)){
            return false;
        }
        if(getPositions(getColorAt(src)).size() == 3){
            return true;
        }

        if(moveUp(src) != null && dest.equals(moveUp(src).getDest())){
            return true;
        }
        if(moveDown(src) != null && dest.equals(moveDown(src).getDest())) {
            return true;
        }
        if(moveRight(src) != null && dest.equals(moveRight(src).getDest())) {
            return true;
        }
        if(moveLeft(src) != null && dest.equals(moveLeft(src).getDest())) {
            return true;
        }
        return false;
    }

    private Move moveUp(Position p) {
        Position dest = getGameBoardPosAt(p).getUp();
        if(dest != null && getColorAt(dest).equals(Options.Color.NOTHING)){
            return new Move(new Position(dest), new Position(p), null);
        }
        return null;
    }

    private Move moveDown(Position p) {
        Position dest = getGameBoardPosAt(p).getDown();
        if(dest != null && getColorAt(dest).equals(Options.Color.NOTHING)){
            return new Move(new Position(dest), new Position(p), null);
        }
        return null;
    }

    private Move moveLeft(Position p) {
        Position dest = getGameBoardPosAt(p).getLeft();
        if(dest != null && getColorAt(dest).equals(Options.Color.NOTHING)){
            return new Move(new Position(dest), new Position(p), null);
        }
        return null;
    }

    private Move moveRight(Position p) {
        Position dest = getGameBoardPosAt(p).getRight();
        if(dest != null && getColorAt(dest).equals(Options.Color.NOTHING)){
            return new Move(new Position(dest), new Position(p), null);
        }
        return null;
    }

    public LinkedList<Position> getAllValidPositions() {
        return allValidPositions;
    }

    GameState getState(Player player) {

        //only the other player can have lost as its impossible for maxPlayer to commit suicide
        if(!movesPossible(player.getOtherPlayer().getColor(), player.getOtherPlayer().getSetCount())){
            return GameState.WON_NO_MOVES;
        }else if ((getPositions(player.getOtherPlayer().getColor()).size() < 3 && player.getOtherPlayer().getSetCount() <= 0)) {
            return GameState.WON_KILLED_ALL;
        }else if(remisCount >= REMISMAX){
            //important to do this last, as remisCount is not reset when the opponent can not make any move
            return GameState.REMIS;
        }

        return GameState.RUNNING;
    }

    public int getRemisCount() {
        return remisCount;
    }

    public String toString() {
        String print = "";

        for(int y = 0; y < LENGTH; y++) {
            String line1 = "";
            String line2 = "";
            String line3 = "";
            for (int x = 0; x < LENGTH; x++) {
                char[] pos1 = {' ',' ',' ', ' ', ' '};
                char[] pos2 = {' ',' ',' ', ' ', ' '};
                char[] pos3 = {' ',' ',' ', ' ', ' '};
                if(getColorAt(x,y).equals(Options.Color.INVALID)){
                    line1 += new String(pos1);
                    line2 += new String(pos2);
                    line3 += new String(pos3);
                    continue;
                }
                if(getColorAt(x,y).equals(Options.Color.BLACK)){
                    pos2[2] = 'B';
                }else if(getColorAt(x,y).equals(Options.Color.WHITE)){
                    pos2[2] = 'W';
                }else if(getColorAt(x,y).equals(Options.Color.NOTHING)){
                    pos2[2] = 'N';
                }
                if(getGameBoardPosAt(x,y).getLeft() != null){
                    pos2[0] = '-';
                    pos2[1] = '-';
                }
                if(getGameBoardPosAt(x,y).getUp() != null){
                    pos1[2] = '|';
                }
                if(getGameBoardPosAt(x,y).getRight() != null){
                    pos2[3] = '-';
                    pos2[4] = '-';
                }
                if(getGameBoardPosAt(x,y).getDown() != null){
                    pos3[2] = '|';
                }
                line1 += new String(pos1);
                line2 += new String(pos2);
                line3 += new String(pos3);
            }
            line1 += '\n';
            line2 += '\n';
            line3 += '\n';
            print += line1 + line2 + line3;
        }
        return print;
    }

}
