package message;

public class Message {
    public int num_client;                  // Number of the client who broadcasted the message.
    public MessageType message_type;        // Type of the message.
    public String[] message_content;        // Content of the message.

    public static enum MessageType {
        ACKNOWLEDGE,
        BROADCAST,
        START,
        END,
        ERROR
    }

    private static String field_separator = " ";
    public static String content_separator = ";";

    public Message(int num_client, MessageType message_type, String[] message){
        this.num_client = num_client;
        this.message_type = message_type;
        this.message_content = message;
    }

    public Message(int num_client, MessageType message_type, String message){
        this.num_client = num_client;
        this.message_type = message_type;
        this.message_content = message.split(content_separator);
    }

    public void changeFieldSeparator(String new_separator){
        field_separator = new_separator;
    }

    public void changeContentSeparator(String new_separator){
        content_separator = new_separator;
    }

    public static String[] parseMessageContent(String content){
        return content.split(content_separator);
    }

    public static Message parseMessage(String message_s){
        Message message;

        String[] message_split = message_s.split(Message.field_separator);
        String[] message_content = parseMessageContent(message_split[2]);

        switch(message_split[0]) {
            case "ACKNOWLEDGE":
                message = new Message(Integer.parseInt(message_split[1]), MessageType.ACKNOWLEDGE, message_content);
                break;
            case "BROADCAST":
                message = new Message(Integer.parseInt(message_split[1]), MessageType.BROADCAST, message_content);
                break;
            case "START":
                message = new Message(Integer.parseInt(message_split[1]), MessageType.START, message_content);
                break;
            case "END":
                message = new Message(Integer.parseInt(message_split[1]), MessageType.END, message_content);
                break;
            default:
                message = new Message(Integer.parseInt(message_split[1]), MessageType.ERROR, new String[0]);
                break;
        }

        return message;
    }

    @Override
    public String toString(){
        String message_s = "";

        switch(message_type) {
            case ACKNOWLEDGE:
                message_s += "ACKNOWLEDGE";
                break;
            case BROADCAST:
                message_s += "BROADCAST";
                break;
            case START:
                message_s += "START";
                break;
            case END:
                message_s += "END";
                break;
            default:
                message_s += "ERROR";
                break;
        }

        message_s += field_separator + num_client + field_separator;

        if(message_content != null){
            for(int i = 0; i < message_content.length; i++){
                message_s += message_content[i];
                if(i != message_content.length - 1){
                    message_s += content_separator;
                }
            }
        }

        return message_s;
    }
}
