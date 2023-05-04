/*
package com.chronology.bot;

import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import com.chronology.bot.service.ChronologyBot;
import com.chronology.bot.service.MessageService;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

public class TelegramBotTest extends AppTest {

    @Mock
    private MessageService messageService;

    //    @Test
    public void testOnUpdateReceived() {
        ChronologyBot bot = Mockito.mock(ChronologyBot.class);
        MessageService messageService = Mockito.mock(MessageService.class);

        Mockito.doCallRealMethod().when(bot).onUpdatesReceived(any());
        Update update = new Update();
        update.setUpdateId(1);

        Message message = new Message();
        Chat chat = new Chat();
        chat.setId(123123L);
        message.setChat(chat);
        message.setText("gg");

        update.setMessage(message);

        bot.onUpdatesReceived(asList(update));
        Mockito.verify(bot).onUpdateReceived(update);
        Mockito.verify(messageService).sendMessage(eq(123123L), eq("Неизвестная команда"));
    }

    //    @Test
    public void tt() {
        ChronologyBot chronologyBot = new ChronologyBot();

        Mockito.doNothing().when(messageService).sendMessage(Mockito.anyLong(), Mockito.anyString());

        Update update = new Update();
        update.setUpdateId(1);

        Message message = new Message();

        User user = new User();
        user.setId(111111L);
        message.setFrom(user);

        Chat chat = new Chat();
        chat.setId(123123L);
        message.setChat(chat);
        message.setText("gg");

        update.setMessage(message);

        chronologyBot.onUpdateReceived(update);
        Mockito.verify(messageService).sendMessage(eq(123123L), eq("Неизвестная команда"));
    }
}
*/
