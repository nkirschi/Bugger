package tech.bugger.control.component;

import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponent;
import javax.faces.component.UINamingContainer;

@FacesComponent("paginator")
public class PaginatorComponent extends UINamingContainer {

    /**
     * The refresh button for the number of entries per page.
     */
    private UIComponent refreshBtn;

    /**
     * Returns the refresh button for the number of entries per page.
     *
     * @return The refresh button for the number of entries per page.
     */
    public UIComponent getRefreshBtn() {
        return refreshBtn;
    }

    /**
     * Sets a new refresh button for the number of entries per page.
     *
     * @param refreshBtn The new refresh button for the number of entries per page.
     */
    public void setRefreshBtn(final UIComponent refreshBtn) {
        this.refreshBtn = refreshBtn;
    }

    /**
     * Returns the client ID of the refresh button for the number of entries per page.
     *
     * @return The client ID of the refresh button for the number of entries per page.
     */
    public String getRefreshBtnID() {
        return refreshBtn.getClientId();
    }

}
