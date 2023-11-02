package main.java.ru.nsu.fit.vinter.lab5.SOCKSproxy.Attachment;

import java.util.Objects;

public class Attachment {
    private final AttachmentType attachmentType;

    public Attachment(AttachmentType attachmentType) {
        this.attachmentType = Objects.requireNonNull(attachmentType, "Attachment type cant be null");
    }

    public AttachmentType getType() {
        return attachmentType;
    }
}
