import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SaveItem implements BurpExtension
{
    private MontoyaApi api;

    @Override
    public void initialize(MontoyaApi api)
    {
        this.api = api;
        api.extension().setName("Save item");


        api.userInterface().registerContextMenuItemsProvider(new ContextMenuItemsProvider()
        {
            @Override
            public List<Component> provideMenuItems(ContextMenuEvent event)
            {
                JMenuItem menuItem = new JMenuItem("Save item");
                menuItem.addActionListener(l ->
                {
                    try
                    {
                        saveItem(event);
                    } catch (IOException e)
                    {
                        throw new RuntimeException(e);
                    }
                });

                return List.of(menuItem);
            }
        });
    }

    private void saveItem(ContextMenuEvent event) throws IOException
    {
        File file = null;

        JFileChooser fileChooser = new JFileChooser();
        int option = fileChooser.showOpenDialog(api.userInterface().swingUtils().suiteFrame());
        if (option == JFileChooser.APPROVE_OPTION)
        {
            file = fileChooser.getSelectedFile();
        }

        List<HttpRequestResponse> requestResponses = event.messageEditorRequestResponse().isPresent() ? List.of(event.messageEditorRequestResponse().get().requestResponse()) : event.selectedRequestResponses();

        if (file != null)
        {
            FileWriter fileWriter = new FileWriter(file, true);

            for (HttpRequestResponse requestResponse : requestResponses)
            {
                fileWriter.write("Request:\r\n" + new String(requestResponse.request().toByteArray().getBytes(), StandardCharsets.UTF_8) + "\r\n");

                if (requestResponse.response() != null)
                {
                    fileWriter.write("Response:\r\n" + new String(requestResponse.response().toByteArray().getBytes(), StandardCharsets.UTF_8) + "\r\n");
                }
            }

            fileWriter.close();
        }
        else
        {
            api.logging().logToError("File is null.");
        }
    }
}
