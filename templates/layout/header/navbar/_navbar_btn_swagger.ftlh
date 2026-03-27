<!--
 * layout > header > navbar > swagger button
 * @included_in : layout/header/navbar/_navbar_base.ftlh
 -->

<div class="app-navbar-item mx-1">
    <div class="d-flex align-items-center">
        <div class="btn btn-icon btn-active-light-primary blink position-relative w-30px h-30px w-md-40px h-md-40px"
             data-bs-toggle="tooltip" data-bs-placement="bottom" data-bs-dismiss="click"
             title="Swagger API Docs"
             onclick="location.replace('/api-docs.html');">
            <span class="svg-primary svg-icon svg-icon-1">
                <i class="bi bi-journal-code fs-2 text-primary"></i>
            </span>
        </div>
    </div>
</div>
