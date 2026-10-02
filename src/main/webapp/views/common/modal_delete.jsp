<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<div class="modal-backdrop"
     id="deleteModal"
     aria-hidden="true"
     data-title-template="<c:out value='${ui.modalTitle}' />"
     data-description-template="<c:out value='${ui.modalDescription}' />">
    <div class="modal"
         role="dialog"
         aria-modal="true"
         aria-labelledby="modalTitle"
         aria-describedby="modalDesc">
        <div class="modal-title" id="modalTitle"></div>
        <div class="modal-desc" id="modalDesc"></div>

        <div class="modal-btns">
            <button type="button"
                    class="modal-btn btn-cancel"
                    data-action="cancel-delete">
                <c:out value="${ui.cancelLabel}" />
            </button>

            <button type="button"
                    class="modal-btn btn-confirm"
                    data-action="confirm-delete">
                <c:out value="${ui.confirmLabel}" />
            </button>
        </div>
    </div>
</div>
