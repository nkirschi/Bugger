package tech.bugger.persistence.gateway;

import tech.bugger.global.transfer.Attachment;
import tech.bugger.global.transfer.Post;
import tech.bugger.global.util.Log;

import java.util.List;

/**
 * Attachment gateway that gives access to post attachments stored in a database.
 */
public class AttachmentDBGateway implements AttachmentGateway {

    private static final Log log = Log.forClass(AttachmentDBGateway.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void create(Attachment attachment) {
        // TODO Auto-generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(Attachment attachment) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(Attachment attachment) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Attachment getContentByID(int id) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Attachment> getAttachmentsForPost(Post post) {
        // TODO Auto-generated method stub
        return null;
    }

}
