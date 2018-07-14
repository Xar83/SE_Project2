package grid;



import com.aws.codestar.projecttemplates.Board;
import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.servlet.AsyncContext;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;


/**
 *
 * @author Ethgar
 */

@ServerEndpoint("/WebSoc")
public class WebSocket{
   private static int cnt = 0;
   private final Set<Session> sessions = new HashSet();
   private final Board gameBoard = new Board();
   
   public WebSocket(){}
   
   public int getCount()
   {
       return cnt;
   }
   
   public void addC()
   {
       cnt++;
   }
   
    @OnOpen
    public void open(Session session) throws IOException 
    {  
        addSession(session);
    }

    @OnClose
    public void close(Session session) 
    {
        removeSession(session);
    }
    
    @OnError
    public void onError(Throwable error) 
    {
        Logger.getLogger(WebSocket.class.getName()).log(Level.SEVERE, null, error);
    }
    
    
    @OnMessage
    public void handleMessage(String message, Session session) throws IOException 
    {
        JsonReader reader = Json.createReader(new StringReader(message));
            JsonObject jsonMessage = reader.readObject();

            if ("click".equals(jsonMessage.getString("action"))) 
            {
//                   int x = (int) jsonMessage.getString("x");
//                   int y = (int) jsonMessage.getString("y");
               
                   
                   int x = jsonMessage.getInt("x");
                   
                   int y = jsonMessage.getInt("y");
                   
                   String color = jsonMessage.getString("color");
                   
                   
                  updateBoard(x,y,color);
            }

//            if ("remove".equals(jsonMessage.getString("action"))) {
//                int id = (int) jsonMessage.getInt("id");
//                sessionH.removeDevice(id);
//            }

//            if ("toggle".equals(jsonMessage.getString("action"))) {
//                int id = (int) jsonMessage.getInt("id");
//                sessionH.toggleDevice(id);
//            }
//        }
    }
    
    
    
    public void addSession(Session session) throws IOException 
    {
        sessions.add(session);
        JsonArray addMessage = createMessage();
        sendToSession(session, addMessage); 
    }
    
    private JsonArray createMessage() {
        JsonArrayBuilder builder = Json.createArrayBuilder();
        builder.add("update");
        for(int i = 1; i < 21; i++)
            {
            for(int j = 1; j < 21; j++)
            {
                builder.add(gameBoard.getSquare(i, j).getColor()); //gameBoard.getSquare(i, j).getColor()
            }
        }
                
        JsonArray addMessage = builder.build();
        return addMessage;
    }
    
    public void updateBoard (int x, int y, String color) throws IOException
    {
        gameBoard.setPosition(x, y, color);
        JsonArray addMessage = createMessage();
        sendToAllSessions(addMessage);
    }
    
    public void startStop (String flag)
    {
        if (flag.equals("start"))
        {
          gameBoard.startStopGame(true);
          gameBoard.runGame();
        }
        else if (flag.equals("stop"))
        {
            gameBoard.startStopGame(false);
        }
    }

    public void removeSession(Session session) 
    {
        sessions.remove(session);
    }
    
    private void sendToAllSessions(JsonArray message) throws IOException 
    {
        for (Session session : sessions) 
        {
            sendToSession(session, message);
        }
    }

    private void sendToSession(Session session, JsonArray message) throws IOException 
    {
            session.getBasicRemote().sendText(message.toString());
    }
}
