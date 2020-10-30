package com.skyll.dev;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class Bot extends TelegramLongPollingBot {
    public static final Logger logger = LoggerFactory.getLogger(Bot.class);
    public static final boolean TEST = true;
    public List<Table> tables = new ArrayList<Table>();
    public Message message;
    public String chipIco = " ⭕️";
    public String attantionIco = "⚠️";
    public String foldIco = "❌";
    public String checkIco = "✅";
    public String errorIco = "❗️";
    public String sayIco = "";
    public String winnerIco = "\uD83C\uDF89 ";


    @Override
    public void onUpdateReceived(Update update) {
        message = update.getMessage();
        if (getTableById(message.getChatId()) == null) {
            tables.add(new Table(message.getChatId()));
        }

        if (message.isGroupMessage()) {
            // delete message if player isn't joined to the game
            if (getTableById(message.getChatId()).isStarted()) {
                if (!isJoinOnGame(message.getFrom().getId())) deleteMsg();
            }



            if (message.getText().startsWith("/create")) {
                doOnCreate();
            }
            else if (message.getText().startsWith("/join")) {
                doOnJoin();
            }
            else if (message.getText().startsWith("/left")) {
                doOnLeft();
            }
            else if (message.getText().startsWith("/cancel")) {
                doOnCancel();
            }
            else if (message.getText().startsWith("/start")) {
                doOnStart();
            }
            /*



                THIS USING IF-ELSE CONSTRUCTION



             */
            else if (message.getText().toLowerCase().startsWith("/raiseto") ||
                    message.getText().toLowerCase().startsWith("/check") ||
                    message.getText().toLowerCase().startsWith("/call") ||
                    message.getText().toLowerCase().startsWith("/allin") ||
                    message.getText().toLowerCase().startsWith("/fold")) {

                Table table = getTableById(message.getChatId());
                // all Message when Game is CREATED and Game is STARTED
                if (getTableById(message.getChatId()).isCreated() && getTableById(message.getChatId()).isStarted() && !getTableById(message.getChatId()).isFinished()) {

                    Player nowPlayer = getTableById(message.getChatId()).getNowPlayerTurn();
                    Player prevPlayer = null;
                    // if message send player whose now turn do that
                    if (message.getFrom().getId() == nowPlayer.getId()) {
                        deleteMsg();
                        boolean isDoIt = false;
                        System.out.println("\n\n" + message.getFrom().getUserName() + ": " + message.getText());

                        // проверяем количество игроков, которые скинули карты или пошли вабанк и прибавляем их к
                        // согласившимся игрокам
                        int countAllInPlayers = 0;
                        int countFoldPlayers = 0;
                        int countDontMovePlayers = 0;
                        for (Player player : table.players) {
                            if (player.getStatus().equals("allin"))
                                countAllInPlayers++;
                            if (player.getStatus().equals("fold"))
                                countFoldPlayers++;
                            if (player.getStatus().equals(""))
                                countDontMovePlayers++;
                        }

                        if (message.getText().toLowerCase().startsWith("/fold")) {
                            nowPlayer.setStatus("fold");
                            table.countAgreedPlayers++;
                            sendMsg(foldIco + " Игрок @" + nowPlayer.getName() + " сбросил карты.");
                            prevPlayer = nowPlayer;
                            isDoIt = true;
                        }

                        else if (message.getText().toLowerCase().startsWith("/check") && nowPlayer.getBet() >= table.nowMaxBet) {
                            nowPlayer.setStatus("check");
                            table.countAgreedPlayers++;
                            sendMsg(checkIco + " Игрок @" + nowPlayer.getName() + " пропускает ход.");
                            prevPlayer = nowPlayer;
                            isDoIt = true;
                        }

                        else if (message.getText().toLowerCase().startsWith("/call") && nowPlayer.getBet() < table.nowMaxBet) {
                            if (nowPlayer.countChips() >= (table.nowMaxBet - nowPlayer.getBet())) {
                                nowPlayer.getChips(table.nowMaxBet - nowPlayer.getBet());
                                nowPlayer.setBet(table.nowMaxBet);
                                nowPlayer.setStatus("call");
                                sendMsg(checkIco + " Игрок @" + nowPlayer.getName() + " принял ставку.");
                            } else {
                                nowPlayer.setBet(nowPlayer.countChips() + nowPlayer.getBet());
                                nowPlayer.getChips(nowPlayer.countChips());
                                nowPlayer.setStatus("allin");
                                sendMsg(attantionIco + " Игрок @" + nowPlayer.getName() + " пошел ВА-БАНК!");
                            }

                            table.countAgreedPlayers++;
                            prevPlayer = nowPlayer;
                            isDoIt = true;
                        }

                        else if (message.getText().toLowerCase().startsWith("/allin")) {
                            nowPlayer.setBet(nowPlayer.countChips() + nowPlayer.getBet());
                            nowPlayer.getChips(nowPlayer.countChips());
                            nowPlayer.setStatus("allin");
                            table.countAgreedPlayers++;

                            if (nowPlayer.getBet() > table.nowMaxBet) {
                                table.nowMaxBet = nowPlayer.getBet();
                                table.countAgreedPlayers = 1 + countFoldPlayers;
                            }

                            sendMsg(attantionIco + " Игрок @" + nowPlayer.getName() + " пошел ВА-БАНК!");
                            prevPlayer = nowPlayer;
                            isDoIt = true;
                        }

                        else if (message.getText().toLowerCase().startsWith("/raiseto")) {
                            int raiseto = 0;

                            try {
                                raiseto = Integer.parseInt(message.getText().replaceAll("/raiseto", "").replaceAll("@" + getBotUsername(), "").replaceAll(" ", ""));
                                if (nowPlayer.countChips() >= raiseto && raiseto > table.nowMaxBet) {
                                    nowPlayer.setStatus("raise");
                                    table.countAgreedPlayers = 1 + countFoldPlayers;
                                    nowPlayer.getChips(raiseto - nowPlayer.getBet());
                                    nowPlayer.setBet(raiseto);
                                    table.nowMaxBet = raiseto;

                                    if (nowPlayer.countChips() == 0) {
                                        sendMsg(attantionIco + " Игрок @" + nowPlayer.getName() + " пошел ВА-БАНК!");
                                    } else {
                                        sendMsg(attantionIco + " Игрок @" + nowPlayer.getName() + " повышает ставку до " + table.nowMaxBet + chipIco + "!");
                                    }

                                    prevPlayer = nowPlayer;
                                    isDoIt = true;
                                } else if (nowPlayer.countChips() < raiseto) {
                                    sendMsg(errorIco + "*У вас недостаточно фишек!*");
                                }
                                else {
                                    sendMsg(errorIco + " Ваша ставка " + raiseto + chipIco + " меньше за ставку стола " + table.nowMaxBet + chipIco);
                                }
                            } catch (NumberFormatException e) {
                                System.out.println("Не могу преобразовать " + raiseto + " в Integer.");
                            }
                        }

                        // Check nextPlayer is the PrevPlayer ? If YES then do next part of game. Else next player's turn
                        if (isDoIt) {
                            try {
                                execute(new DeleteMessage()
                                        .setMessageId(table.callPlayerMsg.getMessageId())
                                        .setChatId(table.callPlayerMsg.getChatId()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            // проверяем количество игроков, которые скинули карты или пошли вабанк и прибавляем их к
                            // согласившимся игрокам
                            countAllInPlayers = 0;
                            countFoldPlayers = 0;
                            countDontMovePlayers = 0;
                            for (Player player : table.players) {
                                if (player.getStatus().equals("allin"))
                                    countAllInPlayers++;
                                if (player.getStatus().equals("fold"))
                                    countFoldPlayers++;
                                if (player.getStatus().equals(""))
                                    countDontMovePlayers++;
                            }


                            if (TEST) {
                                System.out.println("count players " + table.players.size());
                                System.out.println("count allin " + countAllInPlayers);
                                System.out.println("count fold " + countFoldPlayers);
                                System.out.println("count agree " + table.countAgreedPlayers);
                            }



                            // проверяем количество согласившигся игроков.
                            // Либо играем дальше, либо переходим к следующему этапу.
                            // И проверяем на количество активных игроков
                            if (table.countAgreedPlayers < table.players.size() && (table.players.size() - countFoldPlayers > 1 || table.players.size() - countAllInPlayers <= 1 || table.players.size() < countDontMovePlayers)) {
                                /**
                                 *              1
                                 */
                                if (TEST) System.out.println("--in 1");

                                nowPlayer = table.getNextPlayerTurn();
                                while (nowPlayer.getStatus().equals("fold") || nowPlayer.getStatus().equals("allin")) {
                                    nowPlayer = table.getNextPlayerTurn();
                                }

                                table.callPlayerMsg = sendMsg("*Слово за игроком @" + nowPlayer.getName() + " ( " + nowPlayer.countChips() + chipIco + " )*\n" +
                                        "Ставка стола " + table.nowMaxBet + chipIco + "\n"+
                                        "Ваша ставка " + nowPlayer.getBet() + chipIco );
                            }
                            else if (table.players.size() - countFoldPlayers <= 1) {
                                /**
                                 *              2
                                 */
                                if (TEST) System.out.println("--in 2");

                                for (Player player : table.players) {
                                    if (player.getStatus().equals("check") || player.getStatus().equals("call") || player.getStatus().equals("raise") || player.getStatus().equals("allin") || player.getStatus().equals("")) {
                                        moveChipsToBank();
                                        int countWinningChips = 0;
                                        for (Bank bank : table.banks) {
                                            countWinningChips += bank.getChips();
                                        }

                                        sendMsg(winnerIco + "*Игрок @" + player.getName() + " победил!*\nВыигрыш " + countWinningChips + chipIco);
                                        player.getChips(-countWinningChips);
                                        break;

                                    }
                                }

                                startNewPart();
                            }
                            else if (table.players.size() - (countAllInPlayers + countFoldPlayers) <= 1) {
                                /**
                                 *              3
                                 */
                                moveChipsToBank();
                                if (TEST) System.out.println("--in 3");

                                    if (table.flop.size() == 0) {
                                        table.deck.dropCard();
                                        table.flop.add(table.deck.getCard());
                                        table.flop.add(table.deck.getCard());
                                        table.flop.add(table.deck.getCard());
                                    }
                                    if (table.turn == null) {
                                        table.deck.dropCard();
                                        table.turn = table.deck.getCard();
                                    }
                                    if (table.river == null) {
                                        table.deck.dropCard();
                                        table.river = table.deck.getCard();
                                    }
                                    sendTableCards(table.getCardsOnTable());

                                    gameIsFinished();
                            }
                            else {
                                /**
                                 *              4
                                 */
                                // Нужно подвести итоги стола, собрать банк и ПРОВЕРИТЬ на ВЫИГРЫШ
                                if (TEST) System.out.println("--in 4");

                                moveChipsToBank();
                                table.countAgreedPlayers = 0;

                                // Закончили подводить итоги стола

                                boolean isFindNextPlayer = false;
                                table.nowPlayerTurn = 0;
                                nowPlayer = table.getNowPlayerTurn();
                                prevPlayer = nowPlayer;
                                if (nowPlayer.getStatus().equals("fold") || nowPlayer.getStatus().equals("allin") || nowPlayer.countChips() == 0) {
                                    for (int i = 1; i < table.players.size(); i++) {
                                        if (!nowPlayer.getStatus().equals("fold") && !nowPlayer.getStatus().equals("allin") && nowPlayer.countChips() > 0) {
                                            nowPlayer = table.players.get(i);
                                            table.nowPlayerTurn = i;
                                        }
                                    }
                                }

                                /*
                                 *  Выкладываем карты на стол.
                                 *  Если флоп не создан - создаем.
                                 *  Если тёрн не создан - создаем
                                 *  Если ривер не создан - создаем
                                 *  и проверяем на конец игры
                                 */

                                if (table.flop.size() == 0) {
//                                    sendMsg("\n\nВсе игроки сделали ход, банк собран. Продолжаем...");
//                                    sendPlayersInfoWithBanks();

                                    table.deck.dropCard();
                                    table.flop.add(table.deck.getCard());
                                    table.flop.add(table.deck.getCard());
                                    table.flop.add(table.deck.getCard());

                                    sendTableCards(table.getCardsOnTable());

                                    table.callPlayerMsg = sendMsg("*Слово за игроком @" + nowPlayer.getName() + " ( " + nowPlayer.countChips() + chipIco + " )*\n" +
                                            "Ставка стола " + table.nowMaxBet + chipIco + "\n"+
                                            "Ваша ставка " + nowPlayer.getBet() + chipIco );

                                } else if (table.turn == null) {
//                                    sendMsg("\n\nВсе игроки сделали ход, банк собран. Продолжаем...");
//                                    sendPlayersInfoWithBanks();
                                    table.deck.dropCard();
                                    table.turn = table.deck.getCard();

                                    sendTableCards(table.getCardsOnTable());

                                    table.callPlayerMsg = sendMsg("*Слово за игроком @" + nowPlayer.getName() + " ( " + nowPlayer.countChips() + chipIco + " )*\n" +
                                            "Ставка стола " + table.nowMaxBet + chipIco + "\n"+
                                            "Ваша ставка " + nowPlayer.getBet() + chipIco );
                                } else if (table.river == null) {
//                                    sendMsg("\n\nВсе игроки сделали ход, банк собран. Продолжаем...");
//                                    sendPlayersInfoWithBanks();
                                    table.deck.dropCard();
                                    table.river = table.deck.getCard();

                                    sendTableCards(table.getCardsOnTable());

                                    table.callPlayerMsg = sendMsg("*Слово за игроком @" + nowPlayer.getName() + " ( " + nowPlayer.countChips() + chipIco + " )*\n" +
                                            "Ставка стола " + table.nowMaxBet + chipIco + "\n"+
                                            "Ваша ставка " + nowPlayer.getBet() + chipIco );
                                } else {
                                    gameIsFinished();
                                }
                            }
                        }

                    } else deleteMsg();
                } else deleteMsg();
            }


        } else if (message.isUserMessage()) {
            System.out.println(message.getFrom().getUserName() + " :: " + message.getText());

        }
    }



    /**
     *
     *  Methods to word with Messages
     *
     */

    public void doOnLeft() {
        if (getTableById(message.getChatId()).isCreated()) {
            if (!getTableById(message.getChatId()).isStarted()) {
                if (isJoinOnGame(message.getFrom().getId())) {
                    getTableById(message.getChatId()).removePlayer(message.getFrom().getId());
                    deleteMsg();
                    try {
                        execute(new EditMessageText()
                                .setChatId(getTableById(message.getChatId()).titleMessage.getChatId())
                                .setMessageId(getTableById(message.getChatId()).titleMessage.getMessageId())
                                .setText("Пользователь @" + getTableById(message.getChatId()).whoAreStarter.getName() + " создал игру!\nПрисоединились к игре:\n"
                                        + getTableById(message.getChatId()).getPlayersNames()
                                        + "\nНажмите /join, чтобы присоединиться."));
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                } else {
                    deleteMsg();
                }
            } else {
                // Что-то делаем, если игрок уже в игре и хочет уйти
//                getTableById(message.getChatId()).removePlayer(message.getFrom().getId());
            }
        } else sendMsg("К сожалению, в данной ситуации, выход только из группы \uD83D\uDE09\nP.S.: Игра еще не создана.");

    }

    public void doOnStart() {
        // If game is created do next
        if (getTableById(message.getChatId()).isCreated()) {
            if (message.getFrom().getId() == getTableById(message.getChatId()).whoAreStarter.getId() && getTableById(message.getChatId()).isCreated()) {
                if (getTableById(message.getChatId()).players.size() > 1 || TEST) {
                    deleteMsg();
//                    Thread thread = new Thread(getTableById(message.getChatId()));
//                    thread.run();
                    if (!getTableById(message.getChatId()).isStarted()) {
                        try {
                            execute(new DeleteMessage()
                                    .setChatId(getTableById(message.getChatId()).titleMessage.getChatId())
                                    .setMessageId(getTableById(message.getChatId()).titleMessage.getMessageId()));
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                        // shuffle players
                        Collections.shuffle(getTableById(message.getChatId()).players);
                        sendMsg(sayIco + " *Приветствую! Минимальная ставка стола " + getTableById(message.getChatId()).minBet + chipIco + "*\n\n" +
                                attantionIco +  "*Незабывайте смотреть свои карты здесь @HoldemTexasPokerGameBot.*");

                        getTableById(message.getChatId()).setStarted(true);

                        beginningOfTheGame();
                    }
                } else sendMsg("Недостаточно игроков, чтобы начать игру.");
            } else sendMsg("Запустить игру может тот, кто её создал.");
        } else sendMsg("Для начала создайте игру.");
    }

    public void doOnCreate() {
        if (!getTableById(message.getChatId()).isCreated()) {
            System.out.println("Created table " + message.getChatId());
            getTableById(message.getChatId()).clear();
            getTableById(message.getChatId()).whoAreStarter = new Player(message.getFrom().getId(), message.getFrom().getUserName(), 5000);
            getTableById(message.getChatId()).setCreated(true);
            getTableById(message.getChatId()).setStarted(false);
            getTableById(message.getChatId()).titleMessage = sendMsg("Пользователь @" + message.getFrom().getUserName() + " создал игру! \nНажмите /join, чтобы присоединиться.");

            deleteMsg();
        } else {

        }
    }

    public void doOnJoin() {
        if (getTableById(message.getChatId()).isCreated()) {

            if (!isJoinOnGame(message.getFrom().getId()) || TEST) {
                getTableById(message.getChatId()).players.add(new Player(message.getFrom().getId(), message.getFrom().getUserName(), 20000));

                try {
                    execute(new EditMessageText()
                            .setChatId(getTableById(message.getChatId()).titleMessage.getChatId())
                            .setMessageId(getTableById(message.getChatId()).titleMessage.getMessageId())
                            .setText("Пользователь @" + getTableById(message.getChatId()).whoAreStarter.getName() + " создал игру!\nПрисоединились к игре:\n"
                                    + getTableById(message.getChatId()).getPlayersNames()
                                    + "\nНажмите /join, чтобы присоединиться."));
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }

            deleteMsg();
        } else {
            sendMsg("Прежде чем присоединиться к игре - её нужно создать.");
        }
    }

    public void doOnCancel() {
        if (getTableById(message.getChatId()).isCreated()) {
            if (message.getFrom().getId() == getTableById(message.getChatId()).whoAreStarter.getId()) {
                if (!getTableById(message.getChatId()).isStarted()) {
                    try {
                        execute(new DeleteMessage()
                                .setChatId(getTableById(message.getChatId()).titleMessage.getChatId())
                                .setMessageId(getTableById(message.getChatId()).titleMessage.getMessageId()));
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }

                deleteMsg();

                SendMessage sendMessage = new SendMessage()
                        .setChatId(message.getChatId())
                        .setText("Игра отменена!");
                clearButtons(sendMessage);
                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }

                // delete cards from players
                deleteHands(getTableById(message.getChatId()).players);

//                getTableById(message.getChatId()).setCreated(false);
//                getTableById(message.getChatId()).setStarted(false);
//                getTableById(message.getChatId()).players.clear();
//                getTableById(message.getChatId()).whoAreStarter = null;

                for (int i = 0; i < tables.size(); i++) {
                    if (tables.get(i).id == message.getChatId()) {
                        tables.set(i, new Table(message.getChatId()));
                        break;
                    }
                }

            } else {
                sendMsg("Вы не можете отменить игру.");
            }
        } else sendMsg("Вы не можете отменить игру, которая еще не создана!");
    }










    /**
     *
     *  Methods to work with users and tables
     *
     */


    public void moveChipsToBank() {
        Table table = getTableById(message.getChatId());
        table.nowMaxBet = 0;

        // remove players from banks
        for (Player player : table.players) {
            if (player.getStatus().equals("fold")) {
                for (Bank bank : table.banks) {
                    bank.removePlayer(player.getId());
                }
            }
        }

//        for (int i = 0; i < table.banks.size(); i++) {
//            if (i + 1 <= table.banks.size()-1) {
//                boolean isEqualsPlayers = table.banks.get(i).getPlayers().containsAll(table.banks.get(i+1).getPlayers());
//
//                if (isEqualsPlayers) {
//                    table.banks.get(i).setChips(table.banks.get(i).getChips() + table.banks.get(i+1).getChips());
//                    table.banks.get(i+1).setChips(0);
//                    table.banks.get(i+1).getPlayers().clear();
//                    if (i+2 <= table.banks.size()-1) {
//                        table.banks.get(i+1).setChips();
//                    }
//                }
//            }
//        }

        int maxBet = 0;
        for (Player player : table.players)
            if (player.getBet() > maxBet && !player.getStatus().equals("fold")) maxBet = player.getBet();

        int minBet = table.players.get(0).getBet();
        for (Player player : table.players)
            if (player.getBet() < minBet && !player.getStatus().equals("fold")) minBet = player.getBet();

        while (maxBet != 0) {
            ArrayList<Player> players = new ArrayList<Player>();
            int countFoldedPlayersChips = 0;
            for (Player player : table.players) {
                if (player.getBet() > 0) {
                    if (player.getBet() >= minBet && !player.getStatus().equals("fold")) {
                        players.add(player);
                        player.setBet(player.getBet() - minBet);
                    }
                    if (player.getStatus().equals("fold")) {
                        if (player.getBet() <= minBet) {
                            countFoldedPlayersChips += player.getBet();
                            player.setBet(0);
                        } else {
                            countFoldedPlayersChips += minBet;
                            player.setBet(player.getBet() - minBet);
                        }
                    }
                }
            }


            // Создаем банк!

            if (players.size() == 1 && countFoldedPlayersChips == 0) {
                players.get(0).getChips(-maxBet);
                players.get(0).setBet(0);
                sendMsg("*Банк вернул @" + players.get(0).getName() + " " + maxBet + chipIco + "*");
                System.out.println("min bet : " + minBet);
                System.out.println("max bet : " + maxBet);
                minBet = 0;
            }

            if (table.banks.size() == 0) {
                table.banks.add(new Bank(minBet * players.size() + countFoldedPlayersChips, players));
                if (TEST) System.out.println("Created bank " + minBet * players.size() + countFoldedPlayersChips);
            } else {  // remove if (players.size() > 1)
                if (table.banks.get(table.banks.size() - 1).getPlayers().equals(players)) //set chips from last bank and add table chips
                    table.banks.get(table.banks.size() - 1).setChips(table.banks.get(table.banks.size() - 1).getChips() + minBet * players.size() + countFoldedPlayersChips);
                else
                    table.banks.add(new Bank(minBet * players.size() + countFoldedPlayersChips, players));
            }


            minBet = maxBet;
            maxBet = 0;
            for (Player player : table.players) {
                if (player.getBet() > maxBet && !player.getStatus().equals("fold")) maxBet = player.getBet();
                if (player.getBet() < minBet && !player.getStatus().equals("fold")) minBet = player.getBet();
            }
        }
    }

    public void startNewPart() {
        Table table = getTableById(message.getChatId());
        deleteHands(table.players);
        ArrayList<Player> players = new ArrayList<Player>();
        for (int i = 1; i < table.players.size(); i++) {
            if (table.players.get(i).countChips() > 0) {
                players.add(new Player(table.players.get(i).getId(), table.players.get(i).getName(), table.players.get(i).countChips()));
            } else System.out.println(table.players.get(i).getName() + " is out!");
        }
        if (table.players.get(0).countChips() > 0) {
            players.add(new Player(table.players.get(0).getId(), table.players.get(0).getName(), table.players.get(0).countChips()));
        }
        table.players.clear();
        table.players.addAll(players);
        table.banks.clear();
        table.flop.clear();
        table.turn = null;
        table.river = null;


        if (table.players.size() > 1) {
            table.setFinished(false);
            table.nowPlayerTurn = 0;
            sendMsg("Начинаем новую игру!");

            beginningOfTheGame();

        } else {
            sendMsg(winnerIco + "Поздравляем @" + table.players.get(0).getName() + " с победой в данном турнире!");
            table.setCreated(false);
            table.setStarted(false);
            table.setFinished(false);
            table.clear();
        }


    }

    public void gameIsFinished() {
        Table table = getTableById(message.getChatId());
        table.setFinished(true);

        moveChipsToBank();
        sendPlayersInfoWithBanks();

        System.out.println("Пытаюсь найти победителя");

        System.out.println(table.banks.size());
        for (Bank bank : table.banks) {
            if (bank.getChips() > 0) {
                ArrayList<Player> winsPlayers = new ArrayList<Player>();
                int rankCombination = -1;
                String nameCombination = "";

                while (winsPlayers.size() == 0) {
                    rankCombination++;
                    for (Player player : bank.getPlayers()) {
                        switch (rankCombination) {
                            case 0:
                                player.setWinningCombination(Evaluation.getRoyalFlush(table.getCardsOnTable(), player.getHand()));
                                nameCombination = "Роял-Флеш";
                                break;

                            case 1:
                                player.setWinningCombination(Evaluation.getStraightFlush(table.getCardsOnTable(), player.getHand()));
                                nameCombination = "Стрит-флеш";
                                break;

                            case 2:
                                player.setWinningCombination(Evaluation.getFourOfAKind(table.getCardsOnTable(), player.getHand()));
                                nameCombination = "Каре";
                                break;

                            case 3:
                                player.setWinningCombination(Evaluation.getFullHouse(table.getCardsOnTable(), player.getHand()));
                                nameCombination = "Фул-Хаус";
                                break;

                            case 4:
                                player.setWinningCombination(Evaluation.getFlush(table.getCardsOnTable(), player.getHand()));
                                nameCombination = "Флеш";
                                break;

                            case 5:
                                player.setWinningCombination(Evaluation.getStraight(table.getCardsOnTable(), player.getHand()));
                                nameCombination = "Стрит";
                                break;

                            case 6:
                                player.setWinningCombination(Evaluation.getThreeOfAKind(table.getCardsOnTable(), player.getHand()));
                                nameCombination = "Трипс";
                                break;

                            case 7:
                                player.setWinningCombination(Evaluation.getTwoPair(table.getCardsOnTable(), player.getHand()));
                                nameCombination = "Две Пары";
                                break;

                            case 8:
                                player.setWinningCombination(Evaluation.getPair(table.getCardsOnTable(), player.getHand()));
                                nameCombination = "Пару";
                                break;

                            case 9:
                                player.setWinningCombination(Evaluation.getHighestCards(table.getCardsOnTable(), player.getHand()));
                                nameCombination = "Старшая карта";
                                break;
                        }
                        if (player.getWinningCombination().size() > 0)
                            winsPlayers.add(player);
                    }
                }

                // Награждение победителей!
                if (winsPlayers.size() == 1) {
                    int countWinningsChips = bank.getChips();

                    if (rankCombination != 9)
                        sendMsg(winnerIco + "*Победил @" + winsPlayers.get(0).getName() + " собрав " + nameCombination + "!*\nВыигрыш " + countWinningsChips + chipIco + "\n" + winsPlayers.get(0).getHand().get(0).view() + " " + winsPlayers.get(0).getHand().get(1).view());
                    else
                        sendMsg(winnerIco + "*Победил @" + winsPlayers.get(0).getName() + " по Старшей карте " + nameCombination + "!*\nВыигрыш " + countWinningsChips + chipIco + "\n" + winsPlayers.get(0).getHand().get(0).view() + " " + winsPlayers.get(0).getHand().get(1).view());

                    table.getPlayer(winsPlayers.get(0).getId()).getChips(-countWinningsChips);
                } else {
                    String text = "";
                    int maxRankCombinationBy1Card;
                    int maxRankCombinationBy2Card;
                    int numCard1;
                    int numCard2;

                    ArrayList<Player> removePlayers = new ArrayList<Player>();

                    switch (rankCombination) {
                        //royal flush
                        case 0:
                            for (Player winsPlayer : winsPlayers) {
                                text += "@" + winsPlayer.getName() + "  -  " + winsPlayer.getHand().get(0).view() + "  " + winsPlayer.getHand().get(1).view() + "\n";
                            }
                            break;

                        //straight flush
                        case 1:

                            //flush
                        case 4:

                            //straight
                        case 5:
                            maxRankCombinationBy1Card = winsPlayers.get(0).getRankCombination(0);
                            for (Player player : winsPlayers) {
                                if (maxRankCombinationBy1Card > player.getRankCombination(0))
                                    maxRankCombinationBy1Card = player.getRankCombination(0);
                            }
                            for (Player winsPlayer : winsPlayers) {
                                if (winsPlayer.getRankCombination(0) == maxRankCombinationBy1Card)
                                    text += "@" + winsPlayer.getName() + "  -  " + winsPlayer.getHand().get(0).view() + "  " + winsPlayer.getHand().get(1).view() + "\n";
                                else removePlayers.add(winsPlayer);
                            }
                            break;

                        //4 kind
                        case 2:
                            numCard1 = 4;
                            maxRankCombinationBy1Card = winsPlayers.get(0).getRankCombination(0);
                            maxRankCombinationBy2Card = winsPlayers.get(0).getRankCombination(numCard1);
                            for (Player player : winsPlayers) {
                                if (maxRankCombinationBy1Card > player.getRankCombination(0))
                                    maxRankCombinationBy1Card = player.getRankCombination(0);
                                if (maxRankCombinationBy2Card > player.getRankCombination(numCard1))
                                    maxRankCombinationBy2Card = player.getRankCombination(numCard1);
                            }
                            for (Player winsPlayer : winsPlayers) {
                                if (winsPlayer.getRankCombination(0) == maxRankCombinationBy1Card && winsPlayer.getRankCombination(numCard1) == maxRankCombinationBy2Card)
                                    text += "@" + winsPlayer.getName() + "  -  " + winsPlayer.getHand().get(0).view() + "  " + winsPlayer.getHand().get(1).view() + "\n";
                                else removePlayers.add(winsPlayer);
                            }
                            break;

                        //full house
                        case 3:
                            numCard1 = 3;
                            maxRankCombinationBy1Card = winsPlayers.get(0).getRankCombination(0);
                            maxRankCombinationBy2Card = winsPlayers.get(0).getRankCombination(numCard1);
                            for (Player player : winsPlayers) {
                                if (maxRankCombinationBy1Card > player.getRankCombination(0))
                                    maxRankCombinationBy1Card = player.getRankCombination(0);
                                if (maxRankCombinationBy2Card > player.getRankCombination(numCard1))
                                    maxRankCombinationBy2Card = player.getRankCombination(numCard1);
                            }
                            for (Player winsPlayer : winsPlayers) {
                                if (winsPlayer.getRankCombination(0) == maxRankCombinationBy1Card && winsPlayer.getRankCombination(numCard1) == maxRankCombinationBy2Card)
                                    text += "@" + winsPlayer.getName() + "  -  " + winsPlayer.getHand().get(0).view() + "  " + winsPlayer.getHand().get(1).view() + "\n";
                                else removePlayers.add(winsPlayer);
                            }
                            break;

                        //3 Kind
                        case 6:

                            //2pair
                        case 7:
                            numCard1 = 3;
                            numCard2 = 4;
                            maxRankCombinationBy1Card = winsPlayers.get(0).getRankCombination(0);
                            maxRankCombinationBy2Card = winsPlayers.get(0).getRankCombination(numCard1);
                            int maxRankCombinationBy3Card = winsPlayers.get(0).getRankCombination(numCard2);
                            for (Player player : winsPlayers) {
                                if (maxRankCombinationBy1Card > player.getRankCombination(0))
                                    maxRankCombinationBy1Card = player.getRankCombination(0);
                                if (maxRankCombinationBy2Card > player.getRankCombination(numCard1))
                                    maxRankCombinationBy2Card = player.getRankCombination(numCard1);
                                if (maxRankCombinationBy3Card > player.getRankCombination(numCard2))
                                    maxRankCombinationBy3Card = player.getRankCombination(numCard2);
                            }
                            for (Player winsPlayer : winsPlayers) {
                                if (winsPlayer.getRankCombination(0) == maxRankCombinationBy1Card && winsPlayer.getRankCombination(numCard1) == maxRankCombinationBy2Card && winsPlayer.getRankCombination(numCard2) == maxRankCombinationBy3Card)
                                    text += "@" + winsPlayer.getName() + "  -  " + winsPlayer.getHand().get(0).view() + "  " + winsPlayer.getHand().get(1).view() + "\n";
                                else removePlayers.add(winsPlayer);
                            }
                            break;

                        //pair
                        case 8:
                            numCard1 = 2;
                            numCard2 = 3;
                            int numCard3 = 4;
                            maxRankCombinationBy1Card = winsPlayers.get(0).getRankCombination(0);
                            maxRankCombinationBy2Card = winsPlayers.get(0).getRankCombination(numCard1);
                            maxRankCombinationBy3Card = winsPlayers.get(0).getRankCombination(numCard2);
                            int maxRankCombinationBy4Card = winsPlayers.get(0).getRankCombination(numCard3);
                            for (Player player : winsPlayers) {
                                if (maxRankCombinationBy1Card > player.getRankCombination(0))
                                    maxRankCombinationBy1Card = player.getRankCombination(0);
                                if (maxRankCombinationBy2Card > player.getRankCombination(numCard1))
                                    maxRankCombinationBy2Card = player.getRankCombination(numCard1);
                                if (maxRankCombinationBy3Card > player.getRankCombination(numCard2))
                                    maxRankCombinationBy3Card = player.getRankCombination(numCard2);
                                if (maxRankCombinationBy4Card > player.getRankCombination(numCard3))
                                    maxRankCombinationBy4Card = player.getRankCombination(numCard3);
                            }
                            for (Player winsPlayer : winsPlayers) {
                                if (winsPlayer.getRankCombination(0) == maxRankCombinationBy1Card && winsPlayer.getRankCombination(numCard1) == maxRankCombinationBy2Card
                                        && winsPlayer.getRankCombination(numCard2) == maxRankCombinationBy3Card && winsPlayer.getRankCombination(numCard3) == maxRankCombinationBy4Card)
                                    text += "@" + winsPlayer.getName() + "  -  " + winsPlayer.getHand().get(0).view() + "  " + winsPlayer.getHand().get(1).view() + "\n";
                                else removePlayers.add(winsPlayer);
                            }
                            break;

                        //high card
                        case 9:
                            maxRankCombinationBy1Card = winsPlayers.get(0).getRankCombination(0);
                            maxRankCombinationBy2Card = winsPlayers.get(0).getRankCombination(1);
                            maxRankCombinationBy3Card = winsPlayers.get(0).getRankCombination(2);
                            maxRankCombinationBy4Card = winsPlayers.get(0).getRankCombination(3);
                            int maxRankCombinationBy5Card = winsPlayers.get(0).getRankCombination(4);
                            for (Player player : winsPlayers) {
                                if (maxRankCombinationBy1Card > player.getRankCombination(0))
                                    maxRankCombinationBy1Card = player.getRankCombination(0);
                                if (maxRankCombinationBy2Card > player.getRankCombination(1))
                                    maxRankCombinationBy2Card = player.getRankCombination(1);
                                if (maxRankCombinationBy3Card > player.getRankCombination(2))
                                    maxRankCombinationBy3Card = player.getRankCombination(2);
                                if (maxRankCombinationBy4Card > player.getRankCombination(3))
                                    maxRankCombinationBy4Card = player.getRankCombination(3);
                                if (maxRankCombinationBy5Card > player.getRankCombination(4))
                                    maxRankCombinationBy5Card = player.getRankCombination(4);
                            }
                            for (Player winsPlayer : winsPlayers) {
                                if (winsPlayer.getRankCombination(0) == maxRankCombinationBy1Card && winsPlayer.getRankCombination(1) == maxRankCombinationBy2Card
                                        && winsPlayer.getRankCombination(2) == maxRankCombinationBy3Card && winsPlayer.getRankCombination(3) == maxRankCombinationBy4Card && winsPlayer.getRankCombination(4) == maxRankCombinationBy5Card)
                                    text +=  "@" + winsPlayer.getName() + "  -  " + winsPlayer.getHand().get(0).view() + "  " + winsPlayer.getHand().get(1).view() + "\n";
                                else removePlayers.add(winsPlayer);
                            }
                            break;
                    }

                    // remove players who wasn't win

                    winsPlayers.removeAll(removePlayers);

                    String startingText = "";
                    if (winsPlayers.size() > 1) {
                        sendMsg(winnerIco + "*Побеждают игроки собрав " + nameCombination + ":*\n" + text + "\n\nПолучают по " + bank.getChips() / winsPlayers.size() + chipIco);

                        for (Player player : winsPlayers) {
                            table.getPlayer(player.getId()).getChips(-(bank.getChips() / winsPlayers.size()));
                        }

                    } else {
                        sendMsg(winnerIco + "*Победил @" + winsPlayers.get(0).getName() + " собрав " + nameCombination + "!*\nВыигрыш " + bank.getChips() + chipIco + "\n" + winsPlayers.get(0).getHand().get(0).view() + " " + winsPlayers.get(0).getHand().get(1).view());
                        table.getPlayer(winsPlayers.get(0).getId()).getChips(-bank.getChips());
                    }

                    // раздать бабло юзерам
                }

                bank.setChips(0);
            }
        }

        // send users cards to table
        String players = "";
        for (Player player : table.players) {
            if (!player.getStatus().equals("fold"))
                players += "@" + player.getName() + ": " + player.getHand().get(0).view() + " " + player.getHand().get(1).view() + "\n";
        }
        sendMsg("*Карты игроков:*\n" + players);

        startNewPart();
    }

    public void beginningOfTheGame() {
        getTableById(message.getChatId()).deck = new Deck();
        getTableById(message.getChatId()).countAgreedPlayers = 0;
        getTableById(message.getChatId()).deck.shuffle();
//        sendPlayersInfo();
        getTableById(message.getChatId()).dealCards();
        sendHandsToUser();
        sendTableCards(new ArrayList<Card>());

//        SendMessage msg = new SendMessage()
//                .setChatId(message.getChatId())
//                .setText(attantionIco + " Обязательно посмотрите свои карты здесь @HoldemTexasPokerGameBot.");
//        setGameButtons(msg);
//        try {
//            execute(msg);
//        } catch (TelegramApiException e) {
//            e.printStackTrace();
//        }

        // blinds
        Player nowPlayer = getTableById(message.getChatId()).getNowPlayerTurn();
        String player1 = "";
        String player2 = "";
        if (nowPlayer.countChips() > 0) {
            nowPlayer.getChips(getTableById(message.getChatId()).minBet);
//            sendMsg("Игрок @" + nowPlayer.getName() + " сделал обязательную ставку: " + getTableById(message.getChatId()).minBet + " " + chipIco);
            player1 = nowPlayer.getName();
            nowPlayer.setBet(getTableById(message.getChatId()).minBet);
            getTableById(message.getChatId()).nowMaxBet = getTableById(message.getChatId()).minBet;

        }

        nowPlayer = getTableById(message.getChatId()).getNextPlayerTurn();
        if (nowPlayer.countChips() > 0) {
            nowPlayer.getChips(getTableById(message.getChatId()).minBet*2);
//            sendMsg("Игрок @" + nowPlayer.getName() + " сделал обязательную ставку: " + getTableById(message.getChatId()).minBet*2 + " " + chipIco);
            player2 = nowPlayer.getName();
            nowPlayer.setBet(getTableById(message.getChatId()).minBet*2);
            getTableById(message.getChatId()).nowMaxBet = getTableById(message.getChatId()).minBet*2;
        }
        sendMsg("Игроки @" + player1 + " и @" + player2 + " сделали обязательные ставки: " + getTableById(message.getChatId()).minBet + chipIco + " и " + getTableById(message.getChatId()).minBet*2 + chipIco);

        nowPlayer = getTableById(message.getChatId()).getNextPlayerTurn();
        getTableById(message.getChatId()).callPlayerMsg =  sendMsg("*Слово за игроком @" + nowPlayer.getName() + " ( " + nowPlayer.countChips() + chipIco + " )*\n" +
                "Ставка стола " + getTableById(message.getChatId()).nowMaxBet + chipIco + "\n"+
                "Ваша ставка " + nowPlayer.getBet() + chipIco);
    }

    public boolean isJoinOnGame(long id) {
        boolean isJoin = false;
        for (Player player : getTableById(message.getChatId()).players) {
            if ( player.getId() == id) {
                isJoin = true;
                break;
            }
        }

        return isJoin;
    }

    public void sendPlayersInfo() {
        String playersInfo = "";
        for (Player player : getTableById(message.getChatId()).players) {
            playersInfo += "\n@" + player.getName() + " : " + player.countChips() + " ⭕️";
        }

        sendMsg("Состояние игроков:\n" + playersInfo);
    }

    public void sendPlayersInfoWithBanks() {
        String playersInfo = "";
        int countChips = 0;
        for (Player player : getTableById(message.getChatId()).players) {
            playersInfo += "\n@" + player.getName() + " : " + player.countChips() + " ⭕️";
        }

        for (Bank bank : getTableById(message.getChatId()).banks) {
            countChips += bank.getChips();
        }

        String text = "";
        for (Bank bank : getTableById(message.getChatId()).banks) {
            text += bank.getChips() + chipIco + " ";
            for (Player player : bank.getPlayers()) {
                text += "@" + player.getName() + " ";
            }
            text += "\n";
        }

        sendMsg("*Состояние игроков:*\n" + playersInfo + "\n\n" +
                "*В банке: *" + countChips + chipIco + "\n\n" + text);
    }

    public String getPlayersInfoWithBanks() {
        String playersInfo = "";
        int countChips = 0;
        for (Player player : getTableById(message.getChatId()).players) {
            playersInfo += "\n@" + player.getName() + " : " + player.countChips() + " ⭕️";
        }

        for (Bank bank : getTableById(message.getChatId()).banks) {
            countChips += bank.getChips();
        }

        String text = "";
        for (Bank bank : getTableById(message.getChatId()).banks) {
            text += bank.getChips() + chipIco + " ";
            for (Player player : bank.getPlayers()) {
                text += "@" + player.getName() + " ";
            }
            text += "\n";
        }

        return "*Состояние игроков:*\n" + playersInfo + "\n\n" + "*В банке:* " + countChips + chipIco + "\n\n" + text;
    }

    public Message sendMsg(String text) {
        Message msg = new Message();
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(message.getChatId());
        sendMessage.setText(text);
        try {
            msg = execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            System.out.println("-- can't send message to " + message.getFrom().getId());
        }

        return msg;
    }

    public void deleteMsg() {
        DeleteMessage msg = new DeleteMessage();
        msg.setChatId(message.getChatId());
        msg.setMessageId(message.getMessageId());

        try {
            execute(msg);
        } catch (TelegramApiException e) {

        }
    }

    public void sendHandsToUser() {
        for (Player player : getTableById(message.getChatId()).players) {
            SendPhoto photo = new SendPhoto();
            photo.setChatId(player.getId());
            photo.setCaption("Это ваши карты с чата: " + message.getChat().getTitle() +"\n");

            photo.setPhoto(Methods.getCardsPicOnUser(player.getHand(), player.getId()));
            try {
                player.setMsgToClearHands(execute(photo));
            } catch (TelegramApiException e) {
                e.printStackTrace();
                System.out.println("-- can't send message to " + message.getFrom().getId());
            }
        }
    }

    public void sendTableCards(ArrayList<Card> cards) {
        SendPhoto photo = new SendPhoto();
        photo.setChatId(getTableById(message.getChatId()).id);
//        photo.setCaption("Ваши карты здесь: @HoldemTexasPokerGameBot");
        photo.setCaption(getPlayersInfoWithBanks().replace("*", ""));


        photo.setPhoto(Methods.getCardsPicOnTable(cards, getTableById(message.getChatId()).id));
        try {
            execute(photo);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            System.out.println("-- can't send message to " + message.getFrom().getId());
        }
    }

    public void deleteHands(List<Player> players) {
        for (Player player : players) {
            try {
                execute(new DeleteMessage()
                        .setChatId(player.getMsgToClearHands().getChatId())
                        .setMessageId(player.getMsgToClearHands().getMessageId()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Здесь устанавливаются кнопки юзерам
    public void setGameButtons(SendMessage sendMessage) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true)
                .setResizeKeyboard(true)
                .setOneTimeKeyboard(false);

        KeyboardRow keyboard1Row = new KeyboardRow();
        keyboard1Row.add(new KeyboardButton("Check / Call"));

        KeyboardRow keyboard2Row = new KeyboardRow();
        keyboard2Row.add(new KeyboardButton("Raise to x2"));
        keyboard2Row.add(new KeyboardButton("Raise to x5"));
        keyboard2Row.add(new KeyboardButton("All-In"));

        KeyboardRow keyboard3Row = new KeyboardRow();
        keyboard3Row.add(new KeyboardButton("Fold"));

        List<KeyboardRow> keyboardRowList = new ArrayList<KeyboardRow>();
        keyboardRowList.clear();
        keyboardRowList.add(keyboard1Row);
        keyboardRowList.add(keyboard2Row);
        keyboardRowList.add(keyboard3Row);
        replyKeyboardMarkup.setKeyboard(keyboardRowList);
    }

    public void clearButtons(SendMessage sendMessage) {
        ReplyKeyboardRemove replyKeyboard = new ReplyKeyboardRemove();
        sendMessage.setReplyMarkup(replyKeyboard);
    }

    public Table getTableById(long id) {
        Table table = null;
        for (Table t : tables) {
            if (t.id == id) table = t;
        }
        return table;
    }

    public String getLinkOnUser(String name, long id) {

        return "[" + name + "](tg://user?id=" + id + ")";
    }






    @Override
    public String getBotUsername() {
        return "HoldemTexasPokerGameBot";
    }

    @Override
    public String getBotToken() {
        return "1138086951:AAE_-A_E4duWK4tMDUDMFeVUWbq8oXiCvX4";
    }
}
