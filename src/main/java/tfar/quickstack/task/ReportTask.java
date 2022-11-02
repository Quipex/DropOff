package tfar.quickstack.task;

import static tfar.quickstack.util.MessageUtils.red;

import java.util.List;

import net.minecraft.sounds.SoundEvents;
import tfar.quickstack.client.ClientUtils;
import tfar.quickstack.client.RendererCubeTarget;
import tfar.quickstack.client.events.RenderWorldLastEventHandler;
import tfar.quickstack.config.DropOffConfig;

public class ReportTask implements Runnable {

    private final int itemsCounter;
    private final int affectedContainers;
    private final int totalContainers;
    private final List<RendererCubeTarget> rendererCubeTargets;

    public ReportTask(int itemsCounter, int affectedContainers, int totalContainers,
            List<RendererCubeTarget> rendererCubeTargets) {
        this.itemsCounter = itemsCounter;
        this.affectedContainers = affectedContainers;
        this.totalContainers = totalContainers;
        this.rendererCubeTargets = rendererCubeTargets;
    }

    @Override
    public void run() {
        if (DropOffConfig.Client.highlightContainers.get()) {
            RenderWorldLastEventHandler.RendererCube.INSTANCE.draw(rendererCubeTargets);
        }

        if (DropOffConfig.Client.displayMessage.get()) {
            String message = red(String.valueOf(itemsCounter)) +
                    " items moved to " + red(String.valueOf(affectedContainers)) +
                    " containers of " + red(String.valueOf(totalContainers)) +
                    " checked in total.";

            ClientUtils.printToChat(message);
        }

        ClientUtils.playSound(SoundEvents.UI_BUTTON_CLICK);
    }

}
