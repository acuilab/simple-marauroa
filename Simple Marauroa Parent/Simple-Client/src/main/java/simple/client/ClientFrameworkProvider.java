package simple.client;

import java.net.SocketException;
import java.util.Map;
import marauroa.client.BannedAddressException;
import marauroa.client.ClientFramework;
import marauroa.client.TimeoutException;
import marauroa.client.net.PerceptionHandler;
import marauroa.common.game.RPObject;
import marauroa.common.net.InvalidVersionException;

public interface ClientFrameworkProvider extends Runnable {

    /**
     * @return the clientManager
     */
    ClientFramework getClientManager();

    /**
     * Connect to server.
     *
     * @param host host URL
     * @param username username
     * @param password password
     * @param user_character character
     * @param port port
     * @param game_name game name
     * @param version game version
     * @throws SocketException if something goes wrong with connection.
     */
    public void connect(String host, String username, String password,
            String user_character, String port,
            String game_name, String version)
            throws SocketException;

    /**
     * Set email.
     *
     * Meant to separate email as a field that can be asked optionally.
     *
     * @param email
     */
    public void setEmail(String email);

    /**
     * Set the Client manager.
     *
     * @param clientManager client manager to set
     */
    public void setClientManager(ClientFramework clientManager);

    /**
     * Set PerceptionHandler.
     *
     * @param handler PerceptionHandler to set.
     */
    public void setPerceptionHandler(PerceptionHandler handler);

    /**
     * Get PerceptionHandler.
     *
     * @return Perception Handler
     */
    public PerceptionHandler getPerceptionHandler();

    /**
     * Get character name.
     *
     * @return character name
     */
    public String getCharacter();

    /**
     * @return the showWorld
     */
    public boolean isShowWorld();

    /**
     * @param aShowWorld the showWorld to set
     */
    public void setShowWorld(boolean aShowWorld);

    /**
     * @return the chat
     */
    public boolean isChat();

    /**
     * @param aChat the chat to set
     */
    public void setChat(boolean aChat);

    /**
     * @return the port
     */
    public String getPort();

    /**
     * @return the gameName
     */
    public String getGameName();

    /**
     * @return the version
     */
    public String getVersion();

    /**
     * @param port the port to set
     */
    public void setPort(String port);

    /**
     * @param gameName the gameName to set
     */
    public void setGameName(String gameName);

    /**
     * @param version the version to set
     */
    public void setVersion(String version);

    /**
     * @param character the character to set
     */
    public void setCharacter(String character);

    /**
     * @return the host
     */
    public String getHost();

    /**
     * @param host the host to set
     */
    public void setHost(String host);

    /**
     * @return the username
     */
    public String getUsername();

    /**
     * @param username the username to set
     */
    public void setUsername(String username);

    /**
     * @return the password
     */
    public String getPassword();

    /**
     * @param password the password to set
     */
    public void setPassword(String password);

    /**
     * Access player's characters.
     *
     * @return player's characters
     */
    public Map<String, RPObject> getCharacters();

    /**
     * Should default account creation if it doesn't exist?
     *
     * @return true if yes, false otherwise.
     */
    public boolean isCreateDefaultCharacter();

    /**
     * Enable/disable default account creation if it doesn't exist.
     *
     * @param enable the createDefaultCharacter to set
     */
    public void setCreateDefaultCharacter(boolean enable);

    /**
     * @return the connected status. true if logged in, false otherwise.
     */
    public boolean isConnected();

    /**
     * Disconnects the client.
     */
    public void disconnect();

    public boolean chooseCharacter(String character) throws TimeoutException,
            InvalidVersionException, BannedAddressException;

    /**
     * Is account auto creation allowed in the client?
     *
     * @return true if true, false otherwise.
     */
    public boolean isAutoCreation();

    /**
     * Set if auto account creation is allowed in client.
     *
     * @param autocreation the auto creation to set
     */
    public void setAutoCreation(boolean autocreation);
}
